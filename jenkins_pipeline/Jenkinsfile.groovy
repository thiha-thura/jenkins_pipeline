pipeline {
    agent any

    stages {
        stage('Connect to Remote Windows Server') {
            steps {
                script {
                    try {
                        bat '''
                        net use Z: \\\\%ipaddress%\\D$ /user:%username% %password%
                        net use X: \\\\%ipaddress%\\C$   
                        '''
                    } catch (Exception e) {
                        error("Failed to connect to the remote server.")
                    }
                }
            }
        }

        stage('Create Folders and Take Backup') {
            parallel {
                stage('Create Folders') {
                    steps {
                        script {
                            try {
                                bat '''
                                mkdir Z:\\Temp\\20230528
                                mkdir Z:\\Backup\\20230528
                                '''
                            } catch (Exception e) {
                                error("Failed to create the folders.")
                            }
                        }
                    }
                }

                stage('Take Backup') {
                    steps {
                        script {
                            try {
                                bat '''
                                xcopy X:\\inetpub\\wwwroot Z:\\Backup\\20230528\\wwwroot /E /I
                                '''
                            } catch (Exception e) {
                                error("Failed to backup wwwroot to the remote server.")
                            }
                        }
                    }
                }
            }
        }

        stage('Clone Git Repository') {
            steps {
                script {
                    try {
                        bat '''
                        git clone https://github.com/github/welcome-to-github.git Z:\\Temp\\20230528\\repository
                        '''
                    } catch (Exception e) {
                        error("Failed to clone the Git repository from the remote server.")
                    }
                }
            }
        }
            
        stage('Deploy') {
            steps {
                script {
                    try {
                        bat '''
                        xcopy Z:\\Temp\\20230528\\repository X:\\inetpub\\wwwroot /E /I /Y
                        '''
                    } catch (Exception e) {
                        error("Failed to deploy files to the remote server.")
                    }
                }
            }
        }

        stage('Rollback') {
            steps {
                timeout(time: 1, unit: 'MINUTES') {
                    input(message: 'Manual rollback required. Proceed?', ok: 'Rollback')
                }
                script {
                    try {
                        bat '''
                        xcopy Z:\\Backup\\20230528\\wwwroot X:\\inetpub\\wwwroot  /E /I /Y
                        '''
                    } catch (Exception e) {
                        error("Failed to rollback wwwroot on the remote server.")
                    }
                }
            }
        }
    }

    post {
        always {
            script {
                timeout(time: 5, unit: 'SECONDS') {
                    try {
                        input message: 'Manual intervention required. Perform rollback?', ok: 'Rollback', parameters: []
                    } catch (Exception e) {
                        // Handle timeout exception
                        echo "Rollback step skipped due to timeout."
                    } finally {
                        bat '''
                        net use Z: /delete
                        net use X: /delete
                        '''
                    }
                }
            }
        }
    }
}