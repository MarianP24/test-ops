let currentOperation = null;
let isLoading = false;
let statusCheckInterval = null;

function showConfirmDialog(message, callback) {
    document.getElementById('confirmMessage').textContent = message;
    document.getElementById('overlay').style.display = 'block';
    document.getElementById('confirmDialog').style.display = 'block';
    document.getElementById('confirmButton').onclick = callback;
}

function showProgressDialog() {
    // Create progress dialog if it doesn't exist
    if (!document.getElementById('progressDialog')) {
        const progressHTML = `
            <div id="progressDialog" class="confirmation-dialog">
                <h3>Operation in Progress</h3>
                <div class="progress-bar-container">
                    <div id="progressBar" class="progress-bar"></div>
                </div>
                <p id="progressPhase" class="progress-phase">Initializing...</p>
                <p id="progressStatus" class="progress-status">Please wait...</p>
            </div>
        `;
        document.body.insertAdjacentHTML('beforeend', progressHTML);
    }

    document.getElementById('overlay').style.display = 'block';
    document.getElementById('progressDialog').style.display = 'block';
}

function hideProgressDialog() {
    const progressDialog = document.getElementById('progressDialog');
    if (progressDialog) {
        progressDialog.style.display = 'none';
    }
    document.getElementById('overlay').style.display = 'none';
}

function cancelOperation() {
    document.getElementById('overlay').style.display = 'none';
    document.getElementById('confirmDialog').style.display = 'none';
    currentOperation = null;
}

function handleOperation(operation) {
    if (isLoading) return;

    currentOperation = operation;
    const message = operation === 'shutdown'
        ? 'Are you sure you want to stop the application?'
        : 'Are you sure you want to restart the application?';

    showConfirmDialog(message, () => {
        executeOperation(operation);
        cancelOperation();
    });
}

function executeOperation(operation) {
    isLoading = true;

    if (operation === 'restart') {
        showProgressDialog();
    }

    fetch(`/application/${operation}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.text();
        })
        .then(message => {
            console.log('Operation initiated:', message);
            if (operation === 'restart') {
                startStatusCheck();
            } else {
                showNotification('success', `Operation ${operation} completed successfully`);
                isLoading = false;
            }
        })
        .catch(error => {
            console.error('Operation failed:', error);
            showNotification('error', `Failed to ${operation}: ${error.message}`);
            hideProgressDialog();
            isLoading = false;
        });
}

function startStatusCheck() {
    let attempts = 0;
    const maxAttempts = 60; // 30 seconds (500ms * 60)

    if (statusCheckInterval) {
        clearInterval(statusCheckInterval);
    }

    statusCheckInterval = setInterval(() => {
        attempts++;

        fetch('/application/status')
            .then(response => response.json())
            .then(status => {
                updateProgressUI(status);

                if (status.completed || attempts >= maxAttempts) {
                    clearInterval(statusCheckInterval);
                    statusCheckInterval = null;
                    hideProgressDialog();
                    isLoading = false;

                    if (status.completed) {
                        showNotification('success', 'Restart completed successfully');
                    } else {
                        showNotification('warning', 'Restart status check timed out');
                    }
                }
            })
            .catch(error => {
                // Server being down during restart is expected
                updateProgressUI({
                    phase: 'SERVER_RESTARTING',
                    progress: Math.min((attempts / maxAttempts) * 100, 100),
                    message: 'Server is restarting...'
                });

                if (attempts >= maxAttempts) {
                    clearInterval(statusCheckInterval);
                    statusCheckInterval = null;
                    hideProgressDialog();
                    isLoading = false;
                    showNotification('warning', 'Restart status check timed out');
                }
            });
    }, 500);
}

function updateProgressUI(status) {
    const progressBar = document.getElementById('progressBar');
    const progressPhase = document.getElementById('progressPhase');
    const progressStatus = document.getElementById('progressStatus');

    const phases = {
        SHUTTING_DOWN: 'Stopping Services',
        SERVER_RESTARTING: 'Restarting Server',
        STARTING_UP: 'Starting Services',
        COMPLETED: 'Restart Complete'
    };

    progressBar.style.width = `${status.progress || 0}%`;
    progressPhase.textContent = phases[status.phase] || 'Processing';
    progressStatus.textContent = status.message || 'Please wait...';
}

function showNotification(type, message) {
    const notification = document.createElement('div');
    notification.className = `status status-${type === 'success' ? 'success' : 'error'}`;
    notification.textContent = message;

    const container = document.querySelector('.container');
    container.appendChild(notification);

    setTimeout(() => {
        notification.remove();
    }, 5000);
}