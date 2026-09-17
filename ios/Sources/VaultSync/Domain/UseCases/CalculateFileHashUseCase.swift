import Foundation

public final class CalculateFileHashUseCase: Sendable {
    private let cryptoService: any CryptoService

    public init(cryptoService: any CryptoService) {
        self.cryptoService = cryptoService
    }

    public func execute(data: Data) async -> String {
        await cryptoService.calculateSha256(data: data)
    }
}
