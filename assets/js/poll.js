function votePoll(pollId, optionId) {
    const xhr = new XMLHttpRequest();
    const ctx = window.appContextPath || '';
    const meta = document.querySelector('meta[name="csrf-token"]');
    const csrfToken = meta ? meta.getAttribute('content') : '';
    let body = 'pollId=' + encodeURIComponent(pollId) + '&optionId=' + encodeURIComponent(optionId);
    if (csrfToken) body += '&csrfToken=' + encodeURIComponent(csrfToken);

    xhr.open('POST', ctx + '/polls/vote', true);
    xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    
    xhr.onload = function() {
        if (xhr.status === 200) {
            const response = JSON.parse(xhr.responseText);
            if (response.status === 'success') {
                updatePollUI(pollId, response);
            }
        } else if (xhr.status === 409) {
            alert('You have already voted on this poll.');
        } else if (xhr.status === 401) {
            window.location.href = ctx + '/auth/login';
        } else {
            alert('Error voting on poll. Please try again.');
        }
    };
    
    xhr.send(body);
}

function updatePollUI(pollId, data) {
    const totalVotes = data.totalVotes;
    
    // Update total votes text
    const totalVotesElement = document.getElementById('total-votes-' + pollId);
    if (totalVotesElement) {
        totalVotesElement.textContent = 'Live | ' + totalVotes + ' votes';
    }
    
    // Update each option
    data.options.forEach(option => {
        const percent = totalVotes > 0 ? Math.round((option.count / totalVotes) * 100) : 0;
        
        // Update progress bar width
        const progressBar = document.getElementById('progress-' + option.id);
        if (progressBar) {
            progressBar.style.width = percent + '%';
        }
        
        // Update percentage text
        const percentText = document.getElementById('percent-' + option.id);
        if (percentText) {
            percentText.textContent = percent + '%';
        }
    });
}
