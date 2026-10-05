#!groovy
pipeline {
    agent any
    tools {
        jdk 'Jdk21'
    }
    stages {
        stage('Setup Maven Settings') {
            steps {
                script {
                    writeFile file: 'settings.xml', text: '''<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      https://maven.apache.org/xsd/settings-1.0.0.xsd">
  <mirrors>
    <mirror>
      <id>maven-default-http-blocker</id>
      <mirrorOf>dummy</mirrorOf>
      <name>Dummy mirror to override default blocking of http repositories</name>
      <url>http://0.0.0.0/</url>
    </mirror>
  </mirrors>
</settings>'''
                }
            }
        }
        stage('Maven Clean') {
            steps {
                sh 'mvn clean -s settings.xml -U'
            }
        }
        stage('Maven Package'){
            steps {
                sh 'mvn package -s settings.xml -U'
                stash includes: 'target/*.jar', name: 'app-jar' // Stash the .jar file
            }
        }
        stage('Load Properties'){
            steps {
                script {
                    sh 'cat src/main/resources/application.properties'
                    
                    def appName = sh(script: 'grep "^spring.application.name=" src/main/resources/application.properties | cut -d= -f2 || true', returnStdout: true).trim()
                    def serverPort = sh(script: 'grep "^server.port=" src/main/resources/application.properties | cut -d= -f2 || true', returnStdout: true).trim()
                    
                    env.NAME_APP = appName ?: 'corenotificacion-api'
                    env.SERVER_PORT = serverPort ?: '8082'
                }
            }
        }

        stage('Docker Deploy') {
            steps {
                script {
                    def port = env.SERVER_PORT ?: '8082'
                    sh 'docker stop ${NAME_APP} || true'
                    sh 'docker rm ${NAME_APP} || true'
                    sh 'docker build -t ${NAME_APP}:latest .'
                    sh "docker run -d --name \${NAME_APP} -p ${port}:${port} -e SERVER_PORT=${port} -e SPRING_PROFILES_ACTIVE=dev -e TZ=America/Bogota --restart always \${NAME_APP}:latest"
                }
            }
        }



    }
}


