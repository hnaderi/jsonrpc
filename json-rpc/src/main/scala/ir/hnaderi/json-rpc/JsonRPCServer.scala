package ir.hnaderi.jsonrpc

import cats.implicits._
import cats.effect.Concurrent
import org.http4s.Response
import org.http4s.server.websocket.WebSocketBuilder
import fs2.Stream
import Stream._
import fs2.Pipe
import org.http4s.websocket.WebSocketFrame
import fs2.concurrent.Queue
import cats.effect.Timer
import io.circe.syntax._
import io.circe.parser.parse

object JsonRPCServer {
  def websocket[F[_]: Concurrent](
      processor: Pipe[F, JsonRPC.Request, JsonRPC.Response],
      bufferSize: Integer
  ): F[Response[F]] =
    for {
      q <- Queue.bounded[F, JsonRPC.Response](bufferSize)
      process = translate(q).andThen(processor).andThen(q.enqueue)
      output = (q.dequeue)
        .map(_.asJson.noSpaces)
        .map(WebSocketFrame.Text(_))
      res <- WebSocketBuilder[F].build(output, process)
    } yield res

  private def translate[F[_]: Concurrent](
      q: Queue[F, JsonRPC.Response]
  ): Pipe[F, WebSocketFrame, JsonRPC.Request] =
    in =>
      for {
        f <- in
        s <- f match {
          case WebSocketFrame.Text(txt, _) => emit(txt)
          case WebSocketFrame.Close(_)     => ???
          case _                           => empty
        }
        j <- parse(s) match {
          case Left(_)      => eval(q.enqueue1(JsonRPC.Errors.ParseError)).drain
          case Right(value) => emit(value)
        }
        req <- j.as[JsonRPC.Request] match {
          case Right(req) => emit(req)
          case Left(_)    => eval(q.enqueue1(JsonRPC.Errors.InvalidRequest)).drain
        }
      } yield req
}
