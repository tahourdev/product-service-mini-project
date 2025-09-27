// pipeline {
//   agent { label "agent-2" }

//   environment {
//     // ---- Docker image settings ----
//     DOCKER_HUB_REPO           = 'keanghor31/spring-app01'
//     DOCKER_HUB_CREDENTIALS_ID = 'docker-hub-credentials'
//     BASE_VERSION              = '1.0'

//     // ---- GitOps repo (Helm values) ----
//     GITOPS_URL            = 'https://github.com/tahourdev/Jenkins-ArgoCD-GitOps.git'
//     GITOPS_BRANCH         = 'main'
//     GITOPS_DIR            = 'gitops-tmp'
//     DEV_VALUES_FILE       = 'manifests/spring-jpa-helm/values-dev.yaml'
//     GITOPS_CREDENTIALS_ID = 'github-jenkins-tahourdev'

//     // ---- Git commit identity ----
//     GIT_USER_NAME         = 'tahourdev'
//     GIT_USER_EMAIL        = 'enghourheng26@gmail.com'
//   }

//   stages {

//     stage('Checkout app') {
//       steps {
//         checkout scm
//       }
//     }

//     stage('Build Spring Boot') {
//       steps {
//         sh '''
//           chmod +x ./gradlew || true
//           ./gradlew clean build -x test
//         '''
//       }
//     }

//     stage('Build & Push Docker image') {
//       steps {
//         script {
//           def shortSha = sh(returnStdout: true, script: "git rev-parse --short HEAD").trim()
//           def imageTag = "${BASE_VERSION}.${env.BUILD_NUMBER}-${shortSha}"
//           env.IMAGE_TAG = imageTag

//           echo "📦 Building image: ${DOCKER_HUB_REPO}:${imageTag}"
//           def img = docker.build("${DOCKER_HUB_REPO}:${imageTag}")

//           echo "🚀 Pushing image to Docker Hub"
//           docker.withRegistry('', DOCKER_HUB_CREDENTIALS_ID) {
//             img.push(imageTag)
//             img.push("latest")
//           }
//         }
//       }
//     }

//     stage('Trivy scan (image)') {
//       steps {
//         sh '''
//           docker run --rm -v /var/run/docker.sock:/var/run/docker.sock aquasec/trivy image \
//             --severity HIGH,CRITICAL --no-progress --format table \
//             -o trivy-scan-report.txt "$DOCKER_HUB_REPO:$IMAGE_TAG" || echo "Trivy scan failed (non-blocking)"
//         '''
//       }
//     }

//     stage('Update GitOps values-dev.yaml') {
//       steps {
//         withCredentials([usernamePassword(
//           credentialsId: "${GITOPS_CREDENTIALS_ID}",
//           usernameVariable: 'GIT_USER',
//           passwordVariable: 'GIT_TOKEN'
//         )]) {
//           sh '''
//             echo "🧹 Cleaning previous clone"
//             rm -rf $GITOPS_DIR

//             echo "📥 Cloning GitOps repo..."
//             git -c credential.helper="!f() { echo username=$GIT_USER; echo password=$GIT_TOKEN; }; f" \
//                 clone "$GITOPS_URL" -b "$GITOPS_BRANCH" "$GITOPS_DIR"

//             cd "$GITOPS_DIR"
//             git config user.name "$GIT_USER_NAME"
//             git config user.email "$GIT_USER_EMAIL"

//             echo "📝 Updating image tag in $DEV_VALUES_FILE -> $IMAGE_TAG"

//             # ✅ Correct sed command with dynamic IMAGE_TAG variable
//             sed -i -E 's#(^\\s*tag:\\s*).*#\\1"'$IMAGE_TAG'"#' "$DEV_VALUES_FILE"

//             echo "✅ File updated. Here's the result:"
//             grep 'tag:' "$DEV_VALUES_FILE"

//             git add "$DEV_VALUES_FILE"
//             git commit -m "ci(dev): bump image to $DOCKER_HUB_REPO:$IMAGE_TAG" || echo "⚠️ No changes to commit"

//             echo "📤 Pushing changes to GitOps repo..."
//             git -c credential.helper="!f() { echo username=$GIT_USER; echo password=$GIT_TOKEN; }; f" \
//                 push origin HEAD:"$GITOPS_BRANCH"
//           '''
//         }
//       }
//     }

//     stage('Info: Argo CD auto-sync') {
//       steps {
//         echo 'ℹ️ Argo CD will auto-sync when values-dev.yaml is updated in GitOps repo.'
//       }
//     }
//   }

//   post {
//     success {
//       echo '✅ Pipeline completed successfully. Image pushed and GitOps updated.'
//       archiveArtifacts artifacts: 'trivy-scan-report.txt', allowEmptyArchive: true
//     }
//     failure {
//       echo '❌ Pipeline failed. Check above logs.'
//       archiveArtifacts artifacts: 'trivy-scan-report.txt', allowEmptyArchive: true
//     }
//   }
// }

pipeline {
    agent any
    environment {
        DOCKER_REPO = 'keanghor31/spring-app01'
        IMAGE_TAG = "v${BUILD_NUMBER}" 
        GITOPS_REPO = 'https://github.com/tahourdev/gitops-spring-app.git'
        GITOPS_CREDENTIALS_ID = 'github-jenkins-tahourdev'
        DOCKER_CREDENTIALS_ID = 'docker-hub-credentials'
    }
    stages {
        stage('Checkout') {
            steps {
                git url: 'https://github.com/tahourdev/product-service-mini-project.git', branch: 'main'
            }
        }
        stage('Build with Gradle') {
            steps {
                sh './gradlew clean bootJar || { echo "Gradle build failed"; exit 1; }'
            }
        }
        stage('Build Docker Image') {
            steps {
                sh "docker build -t ${DOCKER_REPO}:${IMAGE_TAG} . || { echo 'Docker build failed'; exit 1; }"
            }
        }
        stage('Push Docker Image') {
            steps {
                withDockerRegistry([credentialsId: "${DOCKER_CREDENTIALS_ID}", url: 'https://index.docker.io/v1/']) {
                    sh "docker push ${DOCKER_REPO}:${IMAGE_TAG} || { echo 'Docker push failed'; exit 1; }"
                }
            }
        }
        stage('Update GitOps Repo') {
            steps {
                dir('gitops') {
                    git url: "${GITOPS_REPO}", credentialsId: "${GITOPS_CREDENTIALS_ID}", branch: 'main'
                    sh "sed -i 's|image: ${DOCKER_REPO}:[a-zA-Z0-9]*|image: ${DOCKER_REPO}:${IMAGE_TAG}|g' manifests/app-deployment.yaml || { echo 'Sed command failed'; exit 1; }"
                    sh 'git config user.email "enghourheng26@gmail.com"'
                    sh 'git config user.name "tahourdev"'
                    sh 'git add manifests/app-deployment.yaml'
                    sh 'git commit -m "Update image to ${IMAGE_TAG}" || { echo "Git commit failed"; exit 1; }'
                    sh 'git push origin main || { echo "Git push failed"; exit 1; }'
                }
            }
        }
    }
    post {
        always {
            echo "Build completed. Checking artifacts..."
            sh 'docker images | grep ${DOCKER_REPO} || echo "No local images found"'
            cleanWs()
        }
        failure {
            echo "Pipeline failed. Check the logs for details."
        }
    }
}
