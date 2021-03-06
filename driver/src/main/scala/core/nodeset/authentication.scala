package reactivemongo.core.nodeset

sealed trait Authentication {
  def user: String
  def db: String
}

/**
 * @param db the name of the database
 * @param user the name (or subject for X509) of the user
 * @param password the password for the [[user]] (`None` for X509)
 */
@deprecated("Will be private", "0.14.0")
case class Authenticate(
  db: String,
  user: String,
  password: Option[String]) extends Authentication {

  override def toString = s"Authenticate($db, $user)"
}

sealed trait Authenticating extends Authentication {
  @deprecated("Will be removed", "0.14.0")
  def password: String
}

object Authenticating {
  def unapply(auth: Authenticating): Option[(String, String, Option[String])] =
    auth match {
      case CrAuthenticating(db, user, pass, _) =>
        Some((db, user, Some(pass)))

      case ScramSha1Authenticating(db, user, pass, _, _, _, _, _) =>
        Some((db, user, Some(pass)))

      case X509Authenticating(db, user) =>
        Some((db, user, Option.empty[String]))

      case _ =>
        None
    }
}

case class CrAuthenticating(db: String, user: String, password: String, nonce: Option[String]) extends Authenticating {
  override def toString: String =
    s"Authenticating($db, $user, ${nonce.map(_ => "<nonce>").getOrElse("<>")})"
}

case class ScramSha1Authenticating(
  db: String, user: String, password: String,
  randomPrefix: String, saslStart: String,
  conversationId: Option[Int] = None,
  serverSignature: Option[Array[Byte]] = None,
  step: Int = 0) extends Authenticating {

  override def toString: String =
    s"Authenticating($db, $user})"
}

case class X509Authenticating(db: String, user: String) extends Authenticating {
  def password = "deprecated"
}

case class Authenticated(db: String, user: String) extends Authentication
