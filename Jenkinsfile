#!groovy
pipeline {
    agent any
    tools {
        jdk 'Jdk21'
    }
    stages {
        stage('Maven Clean') {
            steps {
                sh 'mvn clean -Dmaven.wagon.http.allowall=true -Dmaven.resolver.transport=wagon'
            }
        }
        stage('Maven Package'){
            steps {
                sh 'mvn package -Dmaven.wagon.http.allowall=true -Dmaven.resolver.transport=wagon'
                stash includes: 'target/*.jar', name: 'app-jar' // Stash the .jar file
            }
        }
        stage('Load Properties'){
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
            steps {
                sh 'docker compose down || true'
                sh 'docker compose up -d --build'
            }
        }
    }
}
