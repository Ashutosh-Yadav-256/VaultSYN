import Foundation

public protocol FileStorage: Sendable {
    func readFile(at path: String) async throws -> Data
    func writeFile(at path: String, data: Data) async throws -> String
    func deleteFile(at path: String) async throws -> Bool
    func fileExists(at path: String) async -> Bool
    func getFileSize(at path: String) async throws -> Int64
    func copyFile(from sourcePath: String, to destinationPath: String) async throws -> String
}
