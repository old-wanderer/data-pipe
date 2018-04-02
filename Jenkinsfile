#!groovy

pipeline {
    agent { dockerfile true }
    stages {
        stage('Test') {
            steps {
                sh 'gradle test'
            }
        }
    }
}