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
                    
                    def props = new Properties()
                    def propsFile = new File("${WORKSPACE}/src/main/resources/application.properties")
                    if (propsFile.exists()) {
                        propsFile.withInputStream { stream -> props.load(stream) }
                    }
                    env.SERVER_PORT = props.getProperty('server.port', '8082')
                    env.NAME_APP = props.getProperty('spring.application.name', 'corenotificacion-api')
                }
            }
        }
        stage('Docker Deploy') {
            steps {
                sh 'docker compose down || true'
                sh 'docker compose up -d --build'
            }
        }
    }
}


