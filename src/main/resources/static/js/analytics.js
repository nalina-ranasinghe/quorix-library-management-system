document.addEventListener('DOMContentLoaded', function() {
    initializeAnalyticsCharts();
    setupAnalyticsFilters();
});

function initializeAnalyticsCharts() {
    // Usage Trends Chart
    const usageCtx = document.getElementById('usageChart');
    if (usageCtx) {
        const usageChart = new Chart(usageCtx.getContext('2d'), {
            type: 'line',
            data: {
                labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
                datasets: [{
                    label: 'Book Borrows',
                    data: [65, 59, 80, 81, 56, 72],
                    borderColor: '#3B82F6',
                    backgroundColor: 'rgba(59, 130, 246, 0.1)',
                    tension: 0.4,
                    fill: true
                }, {
                    label: 'User Registrations',
                    data: [28, 48, 40, 45, 36, 52],
                    borderColor: '#10B981',
                    backgroundColor: 'rgba(16, 185, 129, 0.1)',
                    tension: 0.4,
                    fill: true
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: {
                        labels: { color: '#E5E7EB' }
                    }
                },
                scales: {
                    x: {
                        grid: { color: 'rgba(255, 255, 255, 0.1)' },
                        ticks: { color: '#E5E7EB' }
                    },
                    y: {
                        grid: { color: 'rgba(255, 255, 255, 0.1)' },
                        ticks: { color: '#E5E7EB' }
                    }
                }
            }
        });
    }

    // User Distribution Chart
    const distributionCtx = document.getElementById('userDistributionChart');
    if (distributionCtx) {
        const distributionChart = new Chart(distributionCtx.getContext('2d'), {
            type: 'doughnut',
            data: {
                labels: ['Students', 'Faculty', 'Staff', 'Public'],
                datasets: [{
                    data: [40, 25, 20, 15],
                    backgroundColor: ['#3B82F6', '#10B981', '#F59E0B', '#EF4444'],
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            color: '#E5E7EB',
                            padding: 20
                        }
                    }
                }
            }
        });
    }
}

function setupAnalyticsFilters() {
    const filterSelects = document.querySelectorAll('.analytics-filter select');
    filterSelects.forEach(select => {
        select.addEventListener('change', function() {
            loadAnalyticsData(this.value);
        });
    });
}

function loadAnalyticsData(timeframe) {
    // Show loading state
    console.log(`Loading analytics for timeframe: ${timeframe} days`);
    // In real implementation, fetch data from server and update charts
}

function exportAnalytics() {
    const exportType = confirm('Export as PDF? Click OK for PDF, Cancel for CSV');
    const timeframe = document.querySelector('select')?.value || '30';

    if (exportType) {
        window.location.href = `/admin/analytics/export/PDF?timeframe=${timeframe}`;
    } else {
        window.location.href = `/admin/analytics/export/CSV?timeframe=${timeframe}`;
    }
}