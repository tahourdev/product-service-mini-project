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
  agent { label "agent-2" }

  environment {
    DOCKER_IMAGE = "keanghor31/spring-app01"
    GIT_MANIFESTS_REPO = "https://github.com/tahourdev/k8s-manifests.git"
    GIT_CREDENTIALS_ID = "github-jenkins-tahourdev"
    DOCKER_CREDENTIALS_ID = "docker-hub-credentials"
  }

  stages {
    stage('Checkout') {
      steps {
        git url: 'https://github.com/tahourdev/product-service-mini-project.git', branch: 'main'
      }
    }

    stage('Build Spring Boot (Maven via Docker)') {
      steps {
        sh 'docker run --rm -v $PWD:/app -w /app maven:3.9.6-openjdk-17 mvn clean package -DskipTests'
      }
    }

    stage('Build and Push Docker') {
      steps {
        script {
          def commitHash = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
          def imageTag = "1.0.${BUILD_NUMBER}-${commitHash}"
          docker.build("${DOCKER_IMAGE}:${imageTag}", ".")
          docker.withRegistry('https://index.docker.io/v1/', DOCKER_CREDENTIALS_ID) {
            docker.image("${DOCKER_IMAGE}:${imageTag}").push()
            docker.image("${DOCKER_IMAGE}:${imageTag}").push('latest')
          }
          env.IMAGE_TAG = imageTag
        }
      }
    }

    stage('Update Manifests') {
      steps {
        script {
          dir('manifests') {
            git url: GIT_MANIFESTS_REPO, branch: 'main', credentialsId: GIT_CREDENTIALS_ID

            sh """
            sed -i 's|image: ${DOCKER_IMAGE}:.*|image: ${DOCKER_IMAGE}:${IMAGE_TAG}|g' spring-app/app-deployment.yaml
            git config user.name 'tahourdev'
            git config user.email 'enghourheng26@gmail.com'
            git add spring-app/app-deployment.yaml
            git commit -m 'Update image tag to ${IMAGE_TAG}' || echo 'No changes to commit'
            git push origin main
            """
          }
        }
      }
    }
  }

  post {
    always {
      cleanWs()
    }
  }
}

