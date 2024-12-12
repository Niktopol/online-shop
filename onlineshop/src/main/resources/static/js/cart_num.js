const counters = document.querySelectorAll(".counter");

const calcPrice = () => {
    let priceElem = document.querySelector("#totprice");
    let price = 0;
    for (const counter of counters){
        price += Number(counter.querySelector("span").textContent) * Number(counter.parentNode.querySelector(".price > span").textContent);
    }
    
    if (isNaN(price)){
        window.location.reload(true);
    } else{
        priceElem.textContent = price.toFixed(2);
    }
}

const altCartAmount = (e, goodId, num) => {
    if (isNaN(goodId)){
        window.location.reload(true);
    }

    let alterGoodAmount = {
        query: "mutation AlterCartGoodAmounts($goods: [GoodAmount!]!) { alterCartGoodAmounts(goods: $goods) }",
        variables: {
          goods: [
            {
              goodId: goodId,
              amount: Number(e.target.parentNode.querySelector("span").textContent) + num
            },
          ]
        }
    }

    if (isNaN(alterGoodAmount.variables.goods[0].amount) || Number(e.target.parentNode.querySelector("span").textContent) <= 0){
        window.location.reload(true);
    }

    if (alterGoodAmount.variables.goods[0].amount > 0){
        fetch("/shop", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(alterGoodAmount)
        }).then(resp => {
            return resp.json();
        }).then(data => {
            if ("errors" in data){
                window.location.reload(true);
            } else {
                e.target.parentNode.querySelector("span").textContent = alterGoodAmount.variables.goods[0].amount;
                calcPrice();
            }
        });
    }
}

for (const counter of counters){
    let goodId = Number(counter.querySelector("span").getAttribute('goodid'));
    let buttons = counter.querySelectorAll("button");
    buttons[0].addEventListener("click", (e) => altCartAmount(e, goodId, -1));
    buttons[1].addEventListener("click", (e) => altCartAmount(e, goodId, 1));
}

calcPrice();