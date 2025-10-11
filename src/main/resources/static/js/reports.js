// Reports Management JavaScript
document.addEventListener('DOMContentLoaded', function() {
    initializeReportFilters();
    setupReportExportHandlers();
});

function initializeReportFilters() {
    // Date range pickers
    const dateInputs = document.querySelectorAll('.date-picker');
    dateInputs.forEach(input => {
        input.addEventListener('change', updateReportPreview);
    });

    // Report type selectors
    const reportSelects = document.querySelectorAll('.report-type-select');
    reportSelects.forEach(select => {
        select.addEventListener('change', updateReportParameters);
    });
}

function setupReportExportHandlers() {
    // PDF export buttons
    const pdfButtons = document.querySelectorAll('.export-pdf');
    pdfButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            exportReport(this.dataset.reportType, 'PDF');
        });
    });

    // CSV export buttons
    const csvButtons = document.querySelectorAll('.export-csv');
    csvButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            exportReport(this.dataset.reportType, 'CSV');
        });
    });
}

function updateReportPreview() {
    const filters = collectReportFilters();

    // Show loading state
    const previewSection = document.getElementById('reportPreview');
    if (previewSection) {
        previewSection.innerHTML = '<div class="loading-skeleton">Generating preview...</div>';
    }

    // Simulate API call
    setTimeout(() => {
        generatePreview(filters);
    }, 1000);
}

function updateReportParameters() {
    const reportType = document.getElementById('reportType').value;
    const paramsSection = document.getElementById('reportParameters');

    if (!paramsSection) return;

    // Update parameters based on report type
    switch (reportType) {
        case 'POPULAR_BOOKS':
            paramsSection.innerHTML = `
                <div class="row g-3">
                    <div class="col-md-6">
                        <label class="form-label">Top N Results</label>
                        <input type="number" class="form-control" id="topN" value="10" min="1" max="100">
                    </div>
                    <div class="col-md-6">
                        <label class="form-label">Minimum Borrow Count</label>
                        <input type="number" class="form-control" id="minBorrows" value="1" min="0">
                    </div>
                </div>
            `;
            break;
        case 'RETURN_TIMELINESS':
            paramsSection.innerHTML = `
                <div class="row g-3">
                    <div class="col-md-6">
                        <label class="form-label">Overdue Threshold (Days)</label>
                        <input type="number" class="form-control" id="overdueDays" value="7" min="1" max="30">
                    </div>
                    <div class="col-md-6">
                        <label class="form-label">Include Renewals</label>
                        <select class="form-select" id="includeRenewals">
                            <option value="true">Yes</option>
                            <option value="false">No</option>
                        </select>
                    </div>
                </div>
            `;
            break;
        default:
            paramsSection.innerHTML = '';
    }
}

function collectReportFilters() {
    const filters = {};

    // Common filters
    const startDate = document.getElementById('startDate')?.value;
    const endDate = document.getElementById('endDate')?.value;
    const reportType = document.getElementById('reportType')?.value;

    if (startDate) filters.startDate = startDate;
    if (endDate) filters.endDate = endDate;
    if (reportType) filters.reportType = reportType;

    // Report-specific filters
    switch (reportType) {
        case 'POPULAR_BOOKS':
            filters.topN = document.getElementById('topN')?.value || 10;
            filters.minBorrows = document.getElementById('minBorrows')?.value || 1;
            break;
        case 'RETURN_TIMELINESS':
            filters.overdueDays = document.getElementById('overdueDays')?.value || 7;
            filters.includeRenewals = document.getElementById('includeRenewals')?.value || 'true';
            break;
    }

    return filters;
}

function exportReport(reportType, format) {
    const filters = collectReportFilters();
    const queryString = new URLSearchParams(filters).toString();

    // Show loading state
    const originalText = event.target.innerHTML;
    event.target.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Generating...';
    event.target.disabled = true;

    // Trigger download
    const downloadUrl = `/admin/reports/export/${reportType}/${format}?${queryString}`;
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.click();

    // Restore button state after a delay
    setTimeout(() => {
        event.target.innerHTML = originalText;
        event.target.disabled = false;
    }, 2000);
}

function generatePreview(filters) {
    const previewSection = document.getElementById('reportPreview');
    if (!previewSection) return;

    // Sample preview data - in real app, this would come from the server
    const previewHtml = `
        <div class="alert alert-info">
            <i class="fas fa-info-circle me-2"></i>
            Preview for ${filters.reportType} report (${filters.startDate} to ${filters.endDate})
        </div>
        <div class="table-responsive">
            <table class="table table-sm table-hover">
                <thead>
                    <tr>
                        <th>Sample Data Column 1</th>
                        <th>Sample Data Column 2</th>
                        <th>Sample Data Column 3</th>
                    </tr>
                </thead>
                <tbody>
                    <tr><td>Sample Row 1</td><td>123</td><td>45.6%</td></tr>
                    <tr><td>Sample Row 2</td><td>456</td><td>78.9%</td></tr>
                    <tr><td>Sample Row 3</td><td>789</td><td>12.3%</td></tr>
                </tbody>
            </table>
        </div>
        <div class="mt-3">
            <small class="text-muted">This is a preview. Actual report may contain more data.</small>
        </div>
    `;

    previewSection.innerHTML = previewHtml;
}

// Email report functionality
function emailReport(reportType) {
    const recipientEmail = prompt('Enter recipient email address:');
    if (!recipientEmail) return;

    const filters = collectReportFilters();
    filters.recipientEmail = recipientEmail;

    fetch('/admin/reports/email', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(filters)
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert('Report sent successfully to ' + recipientEmail);
            } else {
                alert('Failed to send report: ' + data.message);
            }
        })
        .catch(error => {
            console.error('Error sending report:', error);
            alert('Error sending report. Please try again.');
        });
}