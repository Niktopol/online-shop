const statuses = document.querySelectorAll(".statuses");

const setOrderStatus = (e, status, orderId) => {
    if (isNaN(orderId)){
        window.location.reload(true);
    }

    let setOrderStatus = {
        query: "mutation setOrderStatus($id: Int!, $status: Int!) { setOrderStatus(id: $id, status: $status) }",
        variables: {
          id: orderId,
          status: Number(status.value)
        }
    }

    fetch("/shop", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(setOrderStatus)
    }).then(resp => {
        return resp.json();
    }).then(data => {
        if ("errors" in data){
            window.location.reload(true);
        } else {
            const div = e.target.parentNode;
            const last = div.lastElementChild;
            if (last == e.target){
                let p = document.createElement("p");
                p.style="color: #00ff00;"
                p.innerText = "Статус установлен";
                div.appendChild(p);
            }
        }
    });
}

for (const status of statuses){
    let button = status.parentNode.querySelector("button");
    let orderId = Number(button.getAttribute('orderid'));
    button.addEventListener("click", (e) => setOrderStatus(e, status, orderId));
}