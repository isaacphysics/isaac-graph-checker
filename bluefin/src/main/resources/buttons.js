window.onload = function() {

    function handleButton(to) {
        return function(button) {
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
    }

    document.querySelectorAll(".Correct").forEach(handleButton("correct"));
    document.querySelectorAll(".Incorrect").forEach(handleButton("incorrect"));
};