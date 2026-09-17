import Foundation

public final class FileManagerStorage: FileStorage, @unchecked Sendable {
    private let rootDirectory: URL
    private let fileManager = FileManager.default

    public init(rootDirectory: URL? = nil) {
        if let rootDirectory {
            self.rootDirectory = rootDirectory
        } else {
            let appSupport = fileManager.urls(for: .applicationSupportDirectory, in: .userDomainMask).first!
            self.rootDirectory = appSupport.appendingPathComponent("vault", isDirectory: true)
        }

        try? fileManager.createDirectory(at: self.rootDirectory, withIntermediateDirectories: true)
    }

    public func readFile(at path: String) async throws -> Data {
        let fileURL = resolve(path: path)
        guard fileManager.fileExists(atPath: fileURL.path) else {
            throw AppError.storageError("File not found at \(fileURL.path)")
        }
        return try Data(contentsOf: fileURL)
    }

    public func writeFile(at path: String, data: Data) async throws -> String {
        let fileURL = resolve(path: path)
        let tempURL = fileURL.appendingPathExtension("tmp")

        do {
            try data.write(to: tempURL, options: .atomic)
            if fileManager.fileExists(atPath: fileURL.path) {
                try fileManager.removeItem(at: fileURL)
            }
            try fileManager.moveItem(at: tempURL, to: fileURL)
            return fileURL.lastPathComponent
        } catch {
            try? fileManager.removeItem(at: tempURL)
            throw AppError.storageError("Failed writing file: \(error.localizedDescription)")
        }
    }

    public func deleteFile(at path: String) async throws -> Bool {
        let fileURL = resolve(path: path)
        guard fileManager.fileExists(atPath: fileURL.path) else { return false }
        try fileManager.removeItem(at: fileURL)
        return true
    }

    public func fileExists(at path: String) async -> Bool {
        fileManager.fileExists(atPath: resolve(path: path).path)
    }

    public func getFileSize(at path: String) async throws -> Int64 {
        let fileURL = resolve(path: path)
        let attrs = try fileManager.attributesOfItem(atPath: fileURL.path)
        return (attrs[.size] as? NSNumber)?.int64Value ?? 0
    }

    public func copyFile(from sourcePath: String, to destinationPath: String) async throws -> String {
        let srcURL = resolve(path: sourcePath)
        let dstURL = resolve(path: destinationPath)
        guard fileManager.fileExists(atPath: srcURL.path) else {
            throw AppError.storageError("Source file not found at \(srcURL.path)")
        }
        if fileManager.fileExists(atPath: dstURL.path) {
            try fileManager.removeItem(at: dstURL)
        }
        try fileManager.copyItem(at: srcURL, to: dstURL)
        return dstURL.lastPathComponent
    }

    private func resolve(path: String) -> URL {
        if path.hasPrefix("/") {
            return URL(fileURLWithPath: path)
        }
        return rootDirectory.appendingPathComponent(path)
    }
}
