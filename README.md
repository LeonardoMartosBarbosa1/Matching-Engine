
## ▶ Running the Application

Simply run the project and execute the `Main` class. Follow the Syntax below for inputs

## Pegged and Limit Preference Logic
- Every Accepted Order has an OrderID associated with it.

- Limit Orders whose price crosses the bid/offer will act as a Market Order — meaning they will not rest in the book.

- Only Orders that rest in the Order Book affect the Price Levels.

- Pegged Orders, when arriving at first time in the Order Book will have their price replaced to match the best available size-related price, but will have time preference over other orders at the same Price Level.

- When an Order that changes the Best Side related to a Pegged Order is emitted and does not cross (so it rests), all Pegged Orders will have preference over this Limit Order.

- Pegged Orders always maintain time preference among themselves.

### **Example**:
<pre>
limit buy 10 10
pegged bid 20
pegged bid 30
+-------------------------+---------+
I  BID                    |  OFFER  |
+-------------------------+---------+
I  10 @ 10,00 | limit 0   |         |
I  20 @ 10,00 | pegged 1  |         |
I  30 @ 10,00 | pegged 2  |         |
+-------------------------+---------+

limit buy 10 30

+-------------------------+---------+
I  BID                    |  OFFER  |
+-------------------------+---------+
I  20 @ 30,00 | pegged 1  |         |
I  30 @ 30,00 | pegged 2  |         |
I  10 @ 30,00 | limit 3   |         |
I  10 @ 10,00 | limit 0   |         |
+-------------------------+---------+
</pre>

## Order Command Parsing Rules


### 📌 Create Market Order

***Syntax***: `market [side] [quantity]`
  
Creates a market order to immediately buy or sell the specified quantity at the best available price.

---

## 📌 Create Limit Order

***Syntax*** `limit [side] [quantity] [price]`

Creates a limit order with a fixed price and quantity.

---

## 📌 Create Pegged Order

***Syntax*** `pegged [pegType] [quantity]`

Creates a pegged order that tracks the specified reference price (`bid` or `ask`).

---

## 📌 Cancel Order

***Syntax*** `cancel [orderID]`
 
Cancels an existing order using its unique identifier.

---

## 📌 Show Order Book

***Syntax*** `show`
 
Displays the current state of the order book.

---

## 📌 Replace Order

Allows updating an existing order’s quantity, price, or both.

***Syntax*** `replace [orderID] [quantity] [price]`


---

## ⚠ Partial Replacement Rules

If you do **not** want to update a field, use `-` as a placeholder.

### Update Only Price

➡ `11 - 10` Changes **price** to `10` for order ID `11`. Quantity remains the same

### Update Only Quantity

➡ `11 10 -` Changes **quantity** to `10` for order ID `11`. Price remains the same

---

## 📝 Notes

- `side` values: `buy`, `sell`
- `pegType` values: `bid`, `ask`
- `orderID` must reference an existing active order
- `-` indicates the field should remain unchanged in Replace Order
- `break` terminates the Process

---
