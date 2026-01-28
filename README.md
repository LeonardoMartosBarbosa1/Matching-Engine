
## ▶ Running the Application

Simply run the project and execute the `Main` class. Follow the Syntax below for inputs

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
