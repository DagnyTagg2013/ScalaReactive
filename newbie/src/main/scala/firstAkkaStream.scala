/**
  * Created on 6/28/16.
  *
  * INTELLIJ:
  * - SBT project
  *
  *
  * DOC SOURCES:
  *
  * * Akka Stream & Http Experimental Scala Docs v.2.0.2
  * - SECTION 1.2.1 Quickstart; Reactive Tweets
  * - SECTION 1.7.1 Buffers and working with Rate
  *
  * * Webinar on Akka Streams and HTTP
  * - Akka Streams in 20 seconds
  *
  * * Akka Stream Quickstart
  *
  * CODE SOURCES:
  *
  * * copy build.sbt from Activator Akka Http Microservice template, then download dependencies as follows:
  * from Terminal cmd-line in dir of build.sbt file:
  * - sbt relaod
  * - sbt update; to DOWNLOAD dependencies from remote locations!
  * from IntelliJ:
  * - open SBT window on RHS, then Rclick REFRESH External Sources to be able to RESOLVE
  * dependencies
  *
  * *******************************
  * GIT ESSENTIALS & REVIEW:
  * *******************************
  *
  * * to create a NEW local repo to track:
  * - git init .
  *
  * * ALWAYs do git status before a commit!
  *
  * * to rollback ADD of too many files prior to COMMIT:
  * - git reset
  *
  * * to commit files physically deleted (and all other staged changes)
  * - git add -u
  *   git add -u for whole working tree
  *   git add -u . for current directory
  *
  * * to push NEW branch AND track it remotely (typically should name local and remote the same)
  * - git checkout -b <mynewlocalbranch>
  * - git branch
  * * mynewlocalbranch
  * - git push -u <remotealias> <mylocalbranch>:<remotebranch>

  * * to get remote code and create new fix branch (BASED on active local branch):
  * - git clone http://github.com/...
  * - git branch
  * * master
  * - git checkout -b <mylocalfixbranch>
  * - git remote add <remotealias> http://glithub.com/orig-location cloned
  * - git push <remotealias> <mylocalfixbranch>:<remotebranch>
  * OR
  * - IFF <remotealias>=origin, and <mylocalfixbranch> = <remotebranch> = master
  * - git push origin master
  *
  * * SAFE UPSTREAM MERGE WORKFLOW:
  * http://stackoverflow.com/questions/5601931/best-and-safest-way-to-merge-a-git-branch-into-master
  * * ON GitHub Website, create your own REMOTE BRANCH in YOUR REPO; of UPSTREAM repo's MASTER branch!
  * ??? HOW EXACTLY SETUP
  * - git remote add <origin> http://github.com/...MY REMOTE COPY NOT SHARED original!!!!
  * - git clone http://github.com/... (this creates local copy, tracked on active MASTER)
  * - git checkout -b <mylocalfixbranch>  (assume is FROM local active master clone from remote)
  * - do some work
  * * NOW sync with remote master:
  * - git checkout master (local)
  * - git pull origin master (remote to local)
  * - git merge mylocalfixbranch (local)
  * - do some tests
  * - git commit
  * - git push origin master (local to remote)
  * * NOW sync with SHARED REMOTE upstream Repo master branch FROM MY LOCAL fix branch
  * - git remote add <upstream> http: ... orig-location
  * - git checkout <master>
  * - git pull <upstream> <master>
  * - git checkout <mylocalfixbranch>
  * - git merge <master>
  * - test
  * - git commit
  * - git push origin master (local to remote)
  * * FINALLY, merge MYREMOTEREPO-MASTER-branch changes into the REMOTE UPSTREAM-MASTER-branch!
  * ??? HOW EXACTLY SETUP
  * - from LOCAL can do a git push <upstream> <master>:<remotebranch>
  *
  * * pulling code from ANY remote source
  * - git remote add <remotealias> http://github/....
  * - git checkout <mylocalmergebranch>
  * - git pull <remotealias> <remotebranch>
  *
  * *** REBASE vs MERGE EDU!
  * https://www.atlassian.com/git/tutorials/merging-vs-rebasing/conceptual-overview
  *
  */

import akka.stream._
import akka.stream.scaladsl._

object firstAkkaStream extends App {


  /* WEBINAR snippet */
  /*
  val bluePrint = Source().via(flow).map(_ * 2).to(sink)
  val mat: materializer = bluePrint.run()
  val f: Future =  Source.single(1).map(_.toString).runWith(Sink.head)
  */

  /* DOCs snippet */
  implicit val system = ActorSystem("reactive-tweets")
  implicit val materializer = ActorMaterializer()
  val authors: Source[Author, Unit] =
    tweets
      .filter(_.hashtags.contains(akka))
      .map(_.author)
  authors.runWith(Sink.foreach(println))

}

