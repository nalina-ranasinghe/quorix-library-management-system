// Automation Management JavaScript
document.addEventListener('DOMContentLoaded', function() {
    initializeAutomationControls();
    setupAutomationEventListeners();
});

function initializeAutomationControls() {
    // Initialize any automation-specific UI components
    console.log('Automation controls initialized');
}

function setupAutomationEventListeners() {
    // Add event listeners for automation actions
    const runButtons = document.querySelectorAll('.run-now-btn');
    runButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            const reportType = this.dataset.reportType;
            runReportNow(reportType);
        });
    });
}

function runReportNow(reportType) {
    if (!confirm(`Run ${getReportTypeName(reportType)} immediately? This will generate and email the report.`)) {
        return;
    }

    // Show loading state
    const originalText = event.target.innerHTML;
    event.target.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Running...';
    event.target.disabled = true;

    // Simulate API call
    fetch(`/admin/reports/automation/run-now/${reportType}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showAlert('Report generated and sent successfully!', 'success');
            } else {
                showAlert('Failed to generate report: ' + data.message, 'danger');
            }
        })
        .catch(error => {
            console.error('Error running report:', error);
            showAlert('Error running report. Please try again.', 'danger');
        })
        .finally(() => {
            // Restore button state
            event.target.innerHTML = originalText;
            event.target.disabled = false;
        });
}

function editAutomation(configId) {
    // Fetch automation config and open edit modal
    fetch(`/admin/reports/automation/config/${configId}`)
        .then(response => response.json())
        .then(config => {
            openEditModal(config);
        })
        .catch(error => {
            console.error('Error fetching automation config:', error);
            showAlert('Error loading automation configuration', 'danger');
        });
}

function toggleAutomation(configId, enable) {
    const action = enable ? 'enable' : 'disable';

    if (!confirm(`Are you sure you want to ${action} this automated report?`)) {
        return;
    }

    fetch(`/admin/reports/automation/toggle/${configId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ enabled: enable })
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showAlert(`Automation ${action}d successfully!`, 'success');
                // Reload the page to reflect changes
                setTimeout(() => location.reload(), 1000);
            } else {
                showAlert(`Failed to ${action} automation: ${data.message}`, 'danger');
            }
        })
        .catch(error => {
            console.error('Error toggling automation:', error);
            showAlert('Error updating automation status', 'danger');
        });
}

function deleteAutomation(configId) {
    if (!confirm('Are you sure you want to delete this automated report? This action cannot be undone.')) {
        return;
    }

    fetch(`/admin/reports/automation/delete/${configId}`, {
        method: 'DELETE'
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showAlert('Automation deleted successfully!', 'success');
                // Remove the row from the table
                const row = document.querySelector(`[data-config-id="${configId}"]`);
                if (row) row.remove();
            } else {
                showAlert('Failed to delete automation: ' + data.message, 'danger');
            }
        })
        .catch(error => {
            console.error('Error deleting automation:', error);
            showAlert('Error deleting automation', 'danger');
        });
}

function getReportTypeName(reportType) {
    const reportNames = {
        'USAGE_PATTERN': 'Usage Pattern Report',
        'POPULAR_BOOKS': 'Popular Books Report',
        'RETURN_TIMELINESS': 'Return Timeliness Report',
        'BOOK_AVAILABILITY': 'Book Availability Report',
        'STAFF_ATTENDANCE': 'Staff Attendance Report',
        'MEMBERSHIP_GROWTH': 'Membership Growth Report'
    };
    return reportNames[reportType] || reportType;
}

function openEditModal(config) {
    // In a real implementation, this would open a modal with the form pre-filled
    console.log('Editing automation config:', config);
    // For now, redirect to edit page
    window.location.href = `/admin/reports/automation/edit/${config.id}`;
}

function showAlert(message, type) {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-dismissible fade show`;
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;

    document.querySelector('.content-header').after(alertDiv);

    // Auto remove after 5 seconds
    setTimeout(() => {
        if (alertDiv.parentElement) {
            alertDiv.remove();
        }
    }, 5000);
}