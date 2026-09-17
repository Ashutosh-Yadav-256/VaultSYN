import Foundation
import BackgroundTasks

public final class SyncTaskScheduler: @unchecked Sendable {
    public static let shared = SyncTaskScheduler()
    public static let taskId = "com.vaultsync.backgroundsync"

    private var syncController: (any SyncController)?

    private init() {}

    public func configure(syncController: any SyncController) {
        self.syncController = syncController
    }

    public func registerBackgroundTask() {
        BGTaskScheduler.shared.register(forTaskWithIdentifier: Self.taskId, using: nil) { task in
            guard let task = task as? BGProcessingTask else { return }
            self.handleBackgroundTask(task: task)
        }
    }

    public func scheduleNextSync() {
        let request = BGProcessingTaskRequest(identifier: Self.taskId)
        request.requiresNetworkConnectivity = false
        request.requiresExternalPower = false
        request.earliestBeginDate = Date(timeIntervalSinceNow: 15 * 60)

        do {
            try BGTaskScheduler.shared.submit(request)
        } catch {
            print("Could not schedule background sync: \(error)")
        }
    }

    private func handleBackgroundTask(task: BGProcessingTask) {
        scheduleNextSync()

        let workTask = Task {
            guard let syncController = self.syncController else {
                task.setTaskCompleted(success: false)
                return
            }
            do {
                _ = try await syncController.startSync()
                task.setTaskCompleted(success: true)
            } catch {
                task.setTaskCompleted(success: false)
            }
        }

        task.expirationHandler = {
            workTask.cancel()
        }
    }
}
