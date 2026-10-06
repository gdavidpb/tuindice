import Foundation
import ScenarioKit

/// Plain HTTP against the WireMock the app talks to. A transport failure answers status -1.
final class HttpBackend {
    private let baseUrl: String
    private let session: URLSession

    init(baseUrl: String) {
        self.baseUrl = baseUrl
        let configuration = URLSessionConfiguration.ephemeral
        configuration.timeoutIntervalForRequest = 5
        configuration.timeoutIntervalForResource = 5
        configuration.requestCachePolicy = .reloadIgnoringLocalCacheData
        session = URLSession(configuration: configuration)
    }

    func http(method: String, path: String, body: String?, authorization: String?) -> HttpReply {
        guard let url = URL(string: baseUrl + path) else {
            return HttpReply(status: -1, body: "invalid URL: \(baseUrl + path)")
        }

        var request = URLRequest(url: url)
        request.httpMethod = method
        if let authorization { request.setValue(authorization, forHTTPHeaderField: "Authorization") }
        if let body {
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
            request.httpBody = Data(body.utf8)
        }

        let done = DispatchSemaphore(value: 0)
        var reply = HttpReply(status: -1, body: "no response")
        let task = session.dataTask(with: request) { data, response, error in
            if let error {
                reply = HttpReply(status: -1, body: String(describing: error))
            } else if let response = response as? HTTPURLResponse {
                reply = HttpReply(status: Int32(response.statusCode), body: String(data: data ?? Data(), encoding: .utf8) ?? "")
            }
            done.signal()
        }
        task.resume()

        if done.wait(timeout: .now() + 6) == .timedOut {
            task.cancel()
            return HttpReply(status: -1, body: "timed out")
        }
        return reply
    }
}
