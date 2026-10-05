#!groovy
pipeline {
	agent none
      tools {
        jdk 'Jdk21'
      }
      stages {
        stage('Maven Clean') {
            agent { label 'master' }
          steps {
            sh 'mvn clean'
          }
        }
        stage('Maven Package'){
            agent { label 'master' }
            steps {
                sh 'mvn package'
                stash includes: 'target/*.jar', name: 'app-jar' // Stash the .jar file
            }
        }
        stage('Load Properties'){
            agent { label 'master' }
            steps {
                script {
                	sh 'cat src/main/resources/application.properties'
                	
                    properties = readProperties file: 'src/main/resources/application.properties'
                    env.SERVER_PORT = properties['server.port']
                    env.NAME_APP = properties['spring.application.name']
                }
            }
        }
        stage('Docker Deploy') {
            agent { label 'master' }
            steps {
                sh 'docker compose down || true'
                sh 'docker compose up -d --build'
            }
        }
    }
}