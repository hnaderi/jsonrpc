package ir.hnaderi.jsonrpc

import cats.implicits._
import io.circe.Json
import io.circe.Encoder
import io.circe.JsonObject
import io.circe.syntax._
import io.circe.Decoder
import io.circe.HCursor
import io.circe.DecodingFailure
import org.http4s.DecodeFailure
import eu.timepit.refined.types.string.NonEmptyString
import io.circe.refined._
import io.circe.JsonNumber

object JsonRPC {
  sealed trait ClientId extends Serializable with Product

  object ClientId {
    final case class StringId(value: String) extends ClientId
    final case class NumberId(value: Long) extends ClientId

    implicit val decoder: Decoder[ClientId] =
      Decoder[String]
        .map(StringId(_))
        .either(Decoder[Long].map(NumberId(_)))
        .map(_.merge)

    implicit val encoder: Encoder[ClientId] = Encoder.instance {
      case StringId(value) => Json.fromString(value)
      case NumberId(value) => Json.fromLong(value)
    }
  }

  final case class RequestData(
      method: String,
      params: List[Json]
  )

  sealed trait Request extends Serializable with Product
  object Request {
    final case class Invoke(id: ClientId, request: RequestData) extends Request
    final case class Notification(payload: RequestData) extends Request

    private implicit val reqDecoder: Decoder[RequestData] = (c: HCursor) =>
      for {
        method <- c.downField("method").as[NonEmptyString]
        params <- c.downField("params").as[Option[List[Json]]]
      } yield RequestData(method.value, params.getOrElse(List.empty))

    private val version = Decoder.decodeLiteralString["2.0"]

    implicit val decoder: Decoder[Request] = (c: HCursor) =>
      for {
        _ <- c.downField("jsonrpc").as(version)
        id <- c.downField("id").as[Option[ClientId]]
        req <- c.as[RequestData]
        res = id match {
          case Some(id) => Invoke(id, req)
          case None     => Notification(req)
        }
      } yield res
  }

  sealed trait Response extends Serializable with Product

  @SuppressWarnings(Array("org.wartremover.warts.DefaultArguments"))
  object Response {
    final case class Success(
        id: ClientId,
        result: Json
    ) extends Response

    final case class Failure(
        id: Option[ClientId],
        code: Integer,
        message: String,
        data: Option[Json] = None
    ) extends Response

    private val base = JsonObject("jsonrpc" -> Json.fromString("2.0"))

    implicit val encoder: Encoder[Response] = Encoder.instance {
      case Success(id, result) =>
        Json.fromJsonObject(
          base
            .add("id", id.asJson)
            .add("result", result)
        )
      case Failure(id, code, message, data) =>
        Json.fromJsonObject(
          base
            .add("id", id.asJson)
            .add(
              "error",
              Json.obj(
                "code" -> code.asJson,
                "message" -> message.asJson,
                "data" -> data.asJson
              )
            )
        )
    }

  }

  object Errors {
    import Response._

    val ParseError: Failure = Failure(None, -32700, "Parse error")
    val InvalidRequest: Failure = Failure(None, -32600, "Invalid request")

    def MethodNotFound(id: ClientId, name: String): Failure =
      Failure(id.some, -32601, s"Method '$name' not found!")

    def InvalidParameters(id: ClientId, name: String): Failure =
      Failure(id.some, -32602, s"Invalid parameters for method '$name'")

    def InternalError(id: ClientId, method: String): Failure =
      Failure(
        id.some,
        -32603,
        s"Internal error occurred while invoking '$method'!'"
      )
  }
}
