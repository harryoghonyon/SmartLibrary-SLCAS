# COS 202 Project Report  
## Smart Library Circulation & Automation System (SLCAS)

**Institution:** MIVA Open University  
**Course:** COS 202  
**System:** Smart Library Circulation & Automation System  

---

### 1. Description

The Smart Library Circulation & Automation System (SLCAS) is a desktop Java application that helps a university library manage catalogue items, registered users, and borrowing workflows. It was built to demonstrate advanced object-oriented design, classic data structures, student-implemented searching and sorting algorithms, recursion, and an event-driven Swing GUI.

The application opens with a branded main window and four tabs: **View Items**, **Borrow / Return**, **Admin**, and **Search & Sort**. Data is kept in memory while the program runs and can be saved to or loaded from a pipe-delimited text file under `data/library_data.txt`. Import and export through a file-chooser dialog are also supported.

---

### 2. Features

- **Catalogue management:** add Books, Magazines, and Journals; delete items; browse in a colour-coded table  
- **Users:** register users with name and email; track current loans and history  
- **Borrow / return:** enforce availability; auto-lend to the next person in the reservation **Queue** on return  
- **Reservations:** join a waitlist when an item is already on loan  
- **Search:** Linear, Binary, and Recursive search by title, author, type, or ID  
- **Sort:** Selection, Insertion, Merge, and Quick Sort by title, author, or year (GUI dropdown)  
- **Undo:** admin add/delete actions are pushed onto a **Stack** and can be reversed  
- **Hot-item cache:** fixed-size **Array** of recently / frequently accessed item IDs  
- **Reports:** most borrowed items, users with overdue loans (with recursive fine calculation), category distribution  
- **Persistence:** save/load text data; file chooser import/export  
- **Reminders:** Swing `Timer` checks for overdue items and shows a dialog  

---

### 3. Data structures used

| Structure | Role in SLCAS |
|-----------|----------------|
| **ArrayList** | Primary store of `LibraryItem` objects in `LibraryDatabase` |
| **Queue (LinkedList)** | Reservation / waitlist per item ID |
| **Stack** | Undo history for admin operations (add item, delete item, add user) |
| **Array** | Fixed-size cache of most frequently accessed item IDs |
| **HashMap** | Fast lookup of users and reservation queues by ID |

Composition is used throughout: `LibraryDatabase` owns collections of items and users; `UserAccount` owns borrowing history lists; `LibraryManager` owns the database, controllers, and undo stack.

---

### 4. Algorithms chosen and why

**Searching**

1. **Linear search** — default for small catalogues and live “search-as-you-type”; simple and correct for unsorted data.  
2. **Binary search** — used when the list can be ordered by the search field; fewer comparisons on larger sorted catalogues.  
3. **Recursive search** — walks the catalogue with recursive calls; demonstrates recursion on the same match logic (title/author/type/ID).

**Sorting**

1. **Selection sort** — easy to explain; fine for small n.  
2. **Insertion sort** — efficient when the list is nearly sorted.  
3. **Merge sort** — stable \(O(n \log n)\) choice recommended for larger catalogues.  
4. **Quick sort** — fast average-case in-place partitioning; selectable from the GUI for comparison.

The Search & Sort tab lets the user pick both the algorithm and the key (title / author / year), which makes it easy to compare behaviour for the coursework demonstration.

**Recursion (beyond search)**

- Overdue fine: ₦50 per day, computed recursively in `LibraryItem.computeOverdueChargeRecursive`.  
- Category totals: `LibraryDatabase.countByTypeRecursive` counts Books / Magazines / Journals.

---

### 5. Object-oriented design highlights

- Abstract class **`LibraryItem`** with subclasses **`Book`**, **`Magazine`**, **`Journal`**  
- Interface **`Borrowable`** implemented by `LibraryItem`  
- Polymorphic helper **`LibraryManager.processLibraryItem`** accepts any `LibraryItem`  
- Packages: `model`, `controller`, `gui`, `utils` as required  

---

### 6. GUI techniques used

| Technique | Where |
|-----------|--------|
| Custom table cell renderer | `ViewItemsPanel` (available / borrowed / overdue colours) |
| Dynamic components | Admin “Add Item” fields change with Book / Magazine / Journal |
| File chooser | Admin → Save/Load → Import / Export |
| Timer overdue notifications | `MainWindow.startOverdueTimer` |
| Input validation + dialogs | All write actions (year, email, empty fields) |
| Mnemonics & keyboard shortcuts | Alt shortcuts; Ctrl+S / Ctrl+O / Ctrl+Z / Ctrl+Q |
| Tooltips | Buttons, fields, tabs, status bar |

Layouts used: **BorderLayout** (main frame), **GridBagLayout** (forms), **CardLayout** (admin sub-screens), **BoxLayout** / **FlowLayout** for tool rows.

---

### 7. Challenges faced

1. **Binary search with “contains” queries** — classic binary search assumes exact ordered keys. Partial matches required a hybrid: attempt binary location, then expand or fall back to a scan so the feature stayed useful in the GUI.  
2. **Undo semantics** — adding an item pushes a “delete this ID” undo; deleting pushes a full item snapshot to restore. Care was needed so borrow state was preserved on restore.  
3. **Persistence without external libraries** — a simple pipe-delimited text format was designed so Books, Magazines, Journals, users, and queues could round-trip without Gson/Jackson.  
4. **Overdue demonstration** — seed data starts with available items; overdue charges appear after loans pass their due date (or when due dates are set in saved data). The recursive fine logic is ready for that case and is shown in reports when overdue loans exist.

---

### 8. How to run / submit checklist

- [x] Source code folder (`SmartLibrary/src`)  
- [x] Screenshots of GUI (`gui-screenshots/`)  
- [x] Class hierarchy diagram (`CLASS_HIERARCHY.md`)  
- [x] This report (`REPORT.md`)  

```bash
./run.sh
```

---

*End of report (approx. 2–3 pages when printed).*
