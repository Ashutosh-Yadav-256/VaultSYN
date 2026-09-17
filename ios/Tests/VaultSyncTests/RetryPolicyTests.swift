import XCTest
@testable import VaultSync

final class RetryPolicyTests: XCTestCase {

    private var policy: RetryPolicy!

    override func setUp() {
        super.setUp()
        policy = RetryPolicy(baseDelaySeconds: 1.0, maxDelaySeconds: 8.0, maxRetries: 3)
    }

    func testShouldRetryAllowsUpToMaxRetries() {
        XCTAssertTrue(policy.shouldRetry(attempt: 0))
        XCTAssertTrue(policy.shouldRetry(attempt: 1))
        XCTAssertTrue(policy.shouldRetry(attempt: 2))
        XCTAssertFalse(policy.shouldRetry(attempt: 3))
        XCTAssertFalse(policy.shouldRetry(attempt: 4))
    }

    func testCalculateDelayProgression() {
        XCTAssertEqual(policy.calculateDelay(attempt: 0), 1.0)
        XCTAssertEqual(policy.calculateDelay(attempt: 1), 2.0)
        XCTAssertEqual(policy.calculateDelay(attempt: 2), 4.0)
        XCTAssertEqual(policy.calculateDelay(attempt: 3), 8.0)
    }

    func testCalculateDelayClampsToMax() {
        XCTAssertEqual(policy.calculateDelay(attempt: 4), 8.0)
        XCTAssertEqual(policy.calculateDelay(attempt: 10), 8.0)
    }
}
