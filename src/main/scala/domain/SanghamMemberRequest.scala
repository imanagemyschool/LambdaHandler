package domain

/**
  * Created by anil.mathew on 8/4/2017.
  */
case class SanghamMemberRequest(trxState: String, memberList: List[Member])

case class Member(firstName: String, lastName: String, zipCode: String, email: String, homeState: String)