function showPopupMessage(message, isSuccess) {
    const popup = document.createElement('div');
    popup.classList.add('status', isSuccess ? 'status-success' : 'status-error');
    popup.textContent = message;
    document.querySelector('.container').appendChild(popup);
    setTimeout(() => popup.remove(), 3000);
}

async function handleOperation(operation) {
    try {
        // Show immediate feedback that operation started
        if (operation === 'restart') {
            showPopupMessage('Restarting application...', true);
        }

        const response = await fetch(`/application/${operation}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(`Failed to ${operation} application: ${response.status}`);
        }

        // Show appropriate success message
        if (operation === 'shutdown') {
            showPopupMessage('Application shutdown successful', true);
        } else if (operation === 'restart') {
            // Show another message after the restart request is accepted
            setTimeout(() => {
                showPopupMessage('Restart request successful!', true);
            }, 3500); // Wait for the first message to disappear
        }
    } catch (error) {
        showPopupMessage(`Operation ${operation} failed: ${error.message}`, false);
    }
}