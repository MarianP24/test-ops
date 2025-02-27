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
    showProgressDialog();

    if (operation === 'shutdown') {
        // Initial state
        updateProgressUI({
            phase: 'INITIALIZING SHUTDOWN',
            status: 'Preparing for shutdown...',
            progress: 0
        });

        // Sequence of shutdown steps with synchronized delays
        setTimeout(() => {
            updateProgressUI({
                phase: 'SHUTTING_DOWN',
                status: 'Starting graceful shutdown...',
                progress: 20
            });
        }, 0);

        setTimeout(() => {
            updateProgressUI({
                phase: 'SHUTTING_DOWN',
                status: 'Stopping application services...',
                progress: 40
            });
        }, 800);

        setTimeout(() => {
            updateProgressUI({
                phase: 'SHUTTING_DOWN',
                status: 'Closing active connections...',
                progress: 60
            });
        }, 1600);

        setTimeout(() => {
            updateProgressUI({
                phase: 'SHUTTING_DOWN',
                status: 'Finalizing shutdown sequence...',
                progress: 80
            });
        }, 2400);

        setTimeout(() => {
            updateProgressUI({
                phase: 'SHUTDOWN INITIATED',
                status: 'Application is shutting down...',
                progress: 100
            });
        }, 3200);
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
            if (operation === 'shutdown') {
                // Final timeout before closing
                setTimeout(() => {
                    updateProgressUI({
                        phase: 'SHUTDOWN COMPLETED',
                        status: 'Application has been shut down successfully',
                        progress: 100
                    });

                    setTimeout(() => {
                        hideProgressDialog();
                        isLoading = false;
                        showNotification('success', 'Shutdown completed successfully');
                    }, 1000);
                }, 3700);
            } else {
                startStatusCheck(operation);
            }
        })
        .catch(error => {
            if (operation === 'shutdown' &&
                (error.message === 'Failed to fetch' || error.message.includes('connection'))) {
                updateProgressUI({
                    phase: 'SHUTDOWN COMPLETED',
                    status: 'Application shutdown successful',
                    progress: 100
                });
                setTimeout(() => {
                    hideProgressDialog();
                    isLoading = false;
                    showNotification('success', 'Application has been shut down successfully');
                }, 1000);
            } else {
                console.error('Operation failed:', error);
                showNotification('error', `Failed to initiate ${operation}`);
                hideProgressDialog();
                isLoading = false;
            }
        });
}

function startStatusCheck(operation) {
    let attempts = 0;
    const maxAttempts = 120; // 60 seconds (500ms * 120) - increased to account for graceful shutdown

    if (statusCheckInterval) {
        clearInterval(statusCheckInterval);
    }

    statusCheckInterval = setInterval(() => {
        attempts++;

        fetch('/application/status')
            .then(response => response.json())
            .then(status => {
                updateProgressUI({
                    phase: status.phase || `${operation} in progress`,
                    status: status.status || 'Waiting for active requests to complete...'
                });

                if (status.completed || attempts >= maxAttempts) {
                    clearInterval(statusCheckInterval);
                    statusCheckInterval = null;
                    hideProgressDialog();
                    isLoading = false;

                    if (status.completed) {
                        showNotification('success', `${operation} completed successfully`);
                    } else {
                        showNotification('warning',
                            `${operation} is taking longer than expected. ` +
                            'The process will continue in the background.');
                    }
                }
            })
            .catch(error => {
                if (operation === 'shutdown' && error.name === 'TypeError') {
                    // Expected behavior - server is no longer responding
                    clearInterval(statusCheckInterval);
                    statusCheckInterval = null;
                    hideProgressDialog();
                    isLoading = false;
                    showNotification('success', 'Application shutdown completed');
                } else {
                    console.error('Status check failed:', error);
                }
            });
    }, 500);
}


function updateProgressUI(status) {
    const progressBar = document.getElementById('progressBar');
    const progressPhase = document.getElementById('progressPhase');
    const progressStatus = document.getElementById('progressStatus');

    if (status.phase === 'SHUTTING_DOWN' || status.phase === 'SHUTDOWN INITIATED') {
        // Add the animated class for shutdown
        progressBar.classList.add('animated');
        progressBar.style.width = '100%';
    } else {
        // Remove animation for other states
        progressBar.classList.remove('animated');
        // Calculate progress based on phase if available
        const progress = status.progress || 0;
        progressBar.style.width = `${progress}%`;
    }

    if (progressPhase) {
        progressPhase.textContent = status.phase;
    }
    if (progressStatus) {
        progressStatus.textContent = status.status;
    }
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