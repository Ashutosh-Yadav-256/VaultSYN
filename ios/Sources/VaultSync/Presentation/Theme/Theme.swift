import SwiftUI

public enum VaultTheme {
    public static let slate950 = Color(red: 0.035, green: 0.051, blue: 0.086)
    public static let slate900 = Color(red: 0.059, green: 0.090, blue: 0.165)
    public static let slate800 = Color(red: 0.118, green: 0.161, blue: 0.231)
    public static let slate700 = Color(red: 0.200, green: 0.255, blue: 0.333)
    public static let slate400 = Color(red: 0.580, green: 0.639, blue: 0.722)
    public static let slate100 = Color(red: 0.945, green: 0.961, blue: 0.976)

    public static let accentCyan = Color(red: 0.220, green: 0.741, blue: 0.973)
    public static let verifiedEmerald = Color(red: 0.063, green: 0.725, blue: 0.506)
    public static let warningAmber = Color(red: 0.961, green: 0.620, blue: 0.043)
    public static let errorRose = Color(red: 0.957, green: 0.247, blue: 0.369)

    public static let cardBackground = Color(red: 0.086, green: 0.122, blue: 0.188)
    public static let borderSubtle = Color(red: 0.141, green: 0.196, blue: 0.278)
}

public func formatBytes(_ bytes: Int64) -> String {
    if bytes < 1024 { return "\(bytes) B" }
    if bytes < 1024 * 1024 { return "\(bytes / 1024) KB" }
    return String(format: "%.1f MB", Double(bytes) / (1024.0 * 1024.0))
}
