function upvote(btn) {
  const count = btn.nextElementSibling;
  count.innerText = parseInt(count.innerText) + 1;
}

function downvote(btn) {
  const count = btn.previousElementSibling;
  count.innerText = parseInt(count.innerText) - 1;
}
