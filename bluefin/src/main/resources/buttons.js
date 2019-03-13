function handleMoveButton(button) {
    var to = button.className;
    button.onclick = function() {
        var row = button.parentElement.parentElement;
        fetch("markAnswer?to=" + to + "&from=" + row.id, {method: "POST"})
            .then( response => {
                if (!response.ok) {
                    alert("Couldn't save");
                } else {
                    row.remove();
                }
            })
    };
}

window.onload = function() {
    document.querySelectorAll(".correct").forEach(handleMoveButton);
    document.querySelectorAll(".incorrect").forEach(handleMoveButton);
    document.querySelectorAll(".delete").forEach(handleMoveButton);
};