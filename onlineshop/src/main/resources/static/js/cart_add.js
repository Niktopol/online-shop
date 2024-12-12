const buttons = document.querySelectorAll(".notin");

const addToCart = (e, goodId) => {
    if (isNaN(goodId)){
        window.location.reload(true);
    }

    let addGoodsToCart = {
        query: "mutation AddGoodsToCart($ids: [Int!]!) { addGoodsToCart(ids: $ids) }",
        variables: {
          ids: [goodId]
        }
    }

    fetch("/shop", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(addGoodsToCart)
    }).then(resp => {
        return resp.json();
    }).then(data => {
        if ("errors" in data){
            if (data.errors[0].extensions.classification == "BAD_REQUEST"){
                e.target.textContent = "В корзине";
                e.target.className = "in";
                e.target.removeEventListener('click', addToCart);
            } else {
                window.location.reload(true);
            }
        } else {
            e.target.textContent = "В корзине";
            e.target.className = "in";
            e.target.removeEventListener('click', addToCart);
        }
    });
}

if (buttons.length > 0) {
    const getCartGoods = {
        query: "query GetCart {getCart {good {id}}}"
    }

    fetch("/shop", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(getCartGoods)
    }).then(resp => {
        return resp.json();
    }).then(data => {
        if ("errors" in data){
            window.location.reload(true);
        } else {
            for (const button of buttons){
                let goodId = Number(button.getAttribute('goodid'));
                for (const id of data.data.getCart) {
                    if (id.good.id == goodId){
                        button.textContent = "В корзине";
                        button.className = "in";
                        break;
                    }
                }
                button.addEventListener("click", (e) => addToCart(e, goodId));
            }
        }
    })
}