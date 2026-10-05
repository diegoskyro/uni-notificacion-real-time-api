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
                sh 'docker-compose down || docker compose down || true'
                sh 'docker-compose up -d --build || docker compose up -d --build'
            }
        }

    }
}


