package domain

case class SnackRequest(trxState: String, snackMemberList: List[SnackMember])

case class SnackMember(gameDate: String, memberCode: String, memberName: String)
