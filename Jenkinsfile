// @Library("Shared") _

// pipeline {
//     agent { label "agent-2" }

//     environment {
//         DOCKERHUB_REPO = "keanghor31/spring-app01"
//         IMAGE_TAG = "v1.0.${BUILD_NUMBER ?: 'latest'}"
//         CONTAINER_NAME = "spring-app01-api"
//     }

//     stages {

//         stage("Code") {
//             steps {
//                 script {
//                     clone("https://github.com/tahourdev/JPA-hibernetes-01.git", "main")
//                 }
//             }
//         }
        
//         // stage("Build Docker Image") {
//         //     steps {
//         //         script {
//         //             def imageFull = "${DOCKERHUB_REPO}:${IMAGE_TAG}"
//         //             echo "🔧 Building Docker image: ${imageFull}"
//         //             sh "docker build -t ${imageFull} ."
//         //         }
//         //     }
//         // }

//         stage("Build Docker Image") {
//             steps {
//                 script {
//                     docker_build("${DOCKERHUB_REPO}", "${IMAGE_TAG}")
//                 }
//             }
//         }

//         // stage("Push to DockerHub") {
//         //     steps {
//         //         script {
//         //             def imageFull = "${DOCKERHUB_REPO}:${IMAGE_TAG}"
//         //             echo "📦 Tagging and pushing Docker image to DockerHub..."

//         //             withCredentials([
//         //                 usernamePassword(
//         //                     credentialsId: 'docker-hub-credentials',
//         //                     usernameVariable: 'DOCKERHUB_USER',
//         //                     passwordVariable: 'DOCKERHUB_PASS'
//         //                 )
//         //             ]) {
//         //                 sh """
//         //                     echo "$DOCKERHUB_PASS" | docker login -u "$DOCKERHUB_USER" --password-stdin
//         //                     docker push ${imageFull}
//         //                     docker logout
//         //                 """
//         //             }
//         //         }
//         //     }
//         // }

//         stage("Push to DockerHub") {
//             steps {
//                 script {
//                     docker_push("${DOCKERHUB_REPO}", "${IMAGE_TAG}")
//                 }
//             }
//         }

//         // stage("Deploy with Docker Compose") {
//         //     steps {
//         //         script {
//         //             def imageFull = "${DOCKERHUB_REPO}:${IMAGE_TAG}"
//         //             echo "🚀 Checking current deployment state..."

//         //             sh """
//         //                 CURRENT_IMAGE=\$(docker inspect --format='{{.Config.Image}}' $CONTAINER_NAME 2>/dev/null || true)
//         //                 EXPECTED_IMAGE="${imageFull}"

//         //                 if [ "\$CURRENT_IMAGE" = "\$EXPECTED_IMAGE" ]; then
//         //                     echo "✅ Container '$CONTAINER_NAME' is already running with image '\$EXPECTED_IMAGE'. Skipping deployment."
//         //                 else
//         //                     echo "📦 Current image: '\$CURRENT_IMAGE'"
//         //                     echo "📦 Expected image: '\$EXPECTED_IMAGE'"
//         //                     echo "🔄 Deploying container using Docker Compose..."
//         //                     docker compose down || true
//         //                     docker compose up -d
//         //                 fi
//         //             """
//         //         }
//         //     }
//         // }
//     }

//     post {
//         success {
//             script {
//                 def imageFull = "${DOCKERHUB_REPO}:${IMAGE_TAG}"
//                 echo "✅ Successfully built, pushed image, and deployed if needed: ${imageFull}"
//             }
//         }
//         failure {
//             echo "❌ Pipeline failed."
//         }
//     }
// }

@Library("Shared") _

pipeline {
    agent { label "agent-2" }

    environment {
        DOCKERHUB_REPO = "keanghor31/spring-app01"
        IMAGE_TAG = "v1.0.${BUILD_NUMBER ?: 'latest'}"
    }

    stages {
        stage("Code") {
            steps {
                script {
                    clone("https://github.com/tahourdev/JPA-hibernetes-01.git", "main")
                }
            }
        }

        stage("Build Docker Image") {
            steps {
                script {
                    def parts = DOCKERHUB_REPO.tokenize('/')
                    docker_build([
                        dockerhubUser: parts[0],
                        appName: parts[1],
                        tag: IMAGE_TAG
                    ])
                }
            }
        }

        stage("Push to DockerHub") {
            steps {
                script {
                    def parts = DOCKERHUB_REPO.tokenize('/')
                    docker_push([
                        dockerhubUser: parts[0],
                        appName: parts[1],
                        tag: IMAGE_TAG
                    ])
                }
            }
        }

        // stage("Deploy") {
        //     steps {
        //         script {
        //             echo "🚀 Deploying application..."
        //             sh "docker compose up -d"
        //         }
        //     }
        // }
    }

    post {
        success {
            echo "✅ Deployment completed."
        }
        failure {
            echo "❌ Pipeline failed."
        }
    }
}
