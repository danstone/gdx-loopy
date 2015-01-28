(defproject gdx-loopy "0.1.0-SNAPSHOT"
  :description "A libgdx game loop"
  :url "http://github.com/danstone/gdx-loopy"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.badlogicgames.gdx/gdx "1.5.0"]
                 [com.badlogicgames.gdx/gdx-backend-lwjgl "1.5.0"]
                 [com.badlogicgames.gdx/gdx-platform "1.5.0"
                  :classifier "natives-desktop"]]
  :repositories [["sonatype"
                  "https://oss.sonatype.org/content/repositories/releases/"]])
