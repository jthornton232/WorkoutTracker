import service.WorkoutTracker
import console.runMainMenu
import data.addSampleData

fun main() {
    val tracker = WorkoutTracker()
    addSampleData(tracker)
    runMainMenu(tracker)
}
