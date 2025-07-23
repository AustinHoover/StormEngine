pipeline {
    agent any
    tools {
        maven '3.9.6'
    }
    stages {
        stage('Setup') {
            steps {
                sh "chmod +x -R ${env.WORKSPACE}"
                sh "git submodule update --init --recursive" // make sure submodules are also checked out
            }
        }
        stage ('Check Environment') {
            steps {
                sh 'mvn --version'
                sh 'java -version'
                sh 'echo $JAVA_HOME'
                sh 'echo $JAVA_INCLUDE_PATH'
            }
        }
        stage('Build (Engine)') {
            steps {
                sh 'mvn -B -DskipTests clean package'
            }
        }
        stage('Build (Documentation)') {
            steps {
                sh 'cd ./docs && doxygen ./Doxyfile'
                sh 'rm -rf /docs/*'
                sh 'cd ./docs-dist/html && rm -f ./docs.tar.gz'
                sh 'cd ./docs-dist/html && tar -czvf ./docs.tar.gz ./*'
                sh 'cp ./docs-dist/html/docs.tar.gz /docs/docs.tar.gz && cd /docs/ && tar -xzvf ./docs.tar.gz'
            }
        }
        stage('Test') {
            steps {
                wrap(
                    [
                        $class: 'Xvfb',
                        additionalOptions: '',
                        assignedLabels: '',
                        autoDisplayName: true,
                        debug: true,
                        displayNameOffset: 0,
                        installationName: 'Default',
                        parallelBuild: true,
                        screen: '1920x1080x24',
                        timeout: 25
                    ]
                ) {
                    script {
                        sh 'rm -f ./testsuccess'
                        catchError {
                            //-fae "fail-at-end", tells maven to not fail the job until all tests have been run
                            sh 'mvn test -P integration -fae && touch ./testsuccess'
                        }
                        // sh 'if ! grep -q "Errors: 2" ./target/surefire-reports/*; then touch ./testsuccess; fi'
                    }
                }
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml', keepLongStdio: true, testDataPublishers: [[$class:'AttachmentPublisher']]
                }
            }
        }
        stage('DebugTests') {
            when {
                expression {
                    !fileExists('./testsuccess')
                }
            }
            steps {
                wrap(
                    [
                        $class: 'Xvfb',
                        additionalOptions: '',
                        assignedLabels: '',
                        autoDisplayName: true,
                        debug: true,
                        displayNameOffset: 0,
                        installationName: 'Default',
                        parallelBuild: true,
                        screen: '1920x1080x24',
                        timeout: 25
                    ]
                ) {
                    script {
                        sh 'curl https://build.lwjgl.org/addons/lwjglx-debug/lwjglx-debug-1.0.0.jar -v -L > ./lwjglx-debug-1.0.0.jar'
                        sh 'mvn clean test -P integrationDebug -DmaxLogs -fae'
                    }
                }
            }
        }
        stage('Test (Native)') {
            steps {
                script {
                    catchError {
                        sh 'cd ./out/build && ctest --output-junit ./Testing/testRes.xml && cd ../..'
                    }
                }
            }
            post {
                always {
                    junit testResults: 'out/build/Testing/**.xml', keepLongStdio: true, testDataPublishers: [[$class:'AttachmentPublisher']]
                }
            }
        }
    }
}