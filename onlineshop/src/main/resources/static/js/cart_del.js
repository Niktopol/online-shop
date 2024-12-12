const buttons = document.querySelectorAll(".rem");

const delFromCart = (e, goodId) => {
    if (isNaN(goodId)){
        window.location.reload(true);
    }

    let delGoodsFromCart = {
        query: "mutation DelGoodsFromCart($ids: [Int!]!) { delGoodsFromCart(ids: $ids) }",
        variables: {
          ids: [goodId]
        }
    }

    fetch("/shop", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(delGoodsFromCart)
    }).then(resp => {
        return resp.json();
    }).then(data => {
        if ("errors" in data){
            window.location.reload(true);
        } else {
            const div = e.target.parentNode.parentNode;
            if (document.querySelectorAll(".item").length == 1){
                e.target.parentNode.remove();
                let p = document.createElement("p");
                p.id = "empt";
                p.innerText = "Корзина пуста";
                div.prepend(p);
                document.querySelector("body > div > form").remove();
            } else {
                e.target.parentNode.remove();
            }
        }
    });
}


for (const button of buttons){
    let goodId = Number(button.getAttribute('goodid'));
    button.addEventListener("click", (e) => delFromCart(e, goodId));
}
