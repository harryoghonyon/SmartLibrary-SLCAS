# SLCAS — Class Hierarchy Diagram

## UML (Mermaid)

```mermaid
classDiagram
    direction TB

    class Borrowable {
        <<interface>>
        +isAvailable() boolean
        +borrowItem(userId)
        +returnItem()
        +getBorrowerId() String
    }

    class LibraryItem {
        <<abstract>>
        -id: String
        -title: String
        -author: String
        -year: int
        -available: boolean
        -borrowCount: int
        -dueDate: LocalDate
        +getType()* String
        +getExtraInfo()* String
        +computeOverdueCharge() double
    }

    class Book {
        -isbn: String
        -pages: int
    }

    class Magazine {
        -issueNumber: int
        -frequency: String
    }

    class Journal {
        -volume: String
        -doi: String
    }

    class UserAccount {
        -userId: String
        -name: String
        -email: String
        -borrowingHistory: List
        -currentlyBorrowed: List
    }

    class LibraryDatabase {
        -items: ArrayList~LibraryItem~
        -users: Map
        -reservationQueues: Map~Queue~
        -frequentAccessCache: String[]
        +countByTypeRecursive() int
    }

    class LibraryManager {
        -database: LibraryDatabase
        -undoStack: Stack~UndoAction~
        +processLibraryItem()
        +undoLastAdminAction()
    }

    class BorrowController
    class SearchEngine
    class SortEngine
    class FileHandler
    class IDGenerator
    class MainWindow

    Borrowable <|.. LibraryItem
    LibraryItem <|-- Book
    LibraryItem <|-- Magazine
    LibraryItem <|-- Journal
    LibraryDatabase o-- LibraryItem : contains
    LibraryDatabase o-- UserAccount : contains
    LibraryManager o-- LibraryDatabase
    LibraryManager o-- BorrowController
    LibraryManager o-- SearchEngine
    LibraryManager o-- SortEngine
    LibraryManager --> FileHandler
    MainWindow --> LibraryManager
    IDGenerator ..> LibraryManager : used by
```

## Package organisation

| Package | Responsibility |
|---------|----------------|
| `model` | Domain objects and data store |
| `controller` | Business logic, search/sort, borrow workflow |
| `gui` | Swing UI (tabs, tables, dialogs) |
| `utils` | ID generation and file persistence |
