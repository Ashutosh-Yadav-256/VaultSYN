import Foundation

public struct RetryPolicy: Sendable {
    public let baseDelaySeconds: Double
    public let maxDelaySeconds: Double
    public let maxRetries: Int

    public init(baseDelaySeconds: Double = 1.0, maxDelaySeconds: Double = 8.0, maxRetries: Int = 3) {
        self.baseDelaySeconds = baseDelaySeconds
        self.maxDelaySeconds = maxDelaySeconds
        self.maxRetries = maxRetries
    }

    public func shouldRetry(attempt: Int) -> Bool {
        attempt < maxRetries
    }

    public func calculateDelay(attempt: Int) -> Double {
        if attempt <= 0 { return baseDelaySeconds }
        let factor = Double(1 << attempt)
        return min(baseDelaySeconds * factor, maxDelaySeconds)
    }
}
