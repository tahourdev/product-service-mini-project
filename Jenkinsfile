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
    // Credentials (Jenkins will expose *_USR and *_PSW vars)
    DOCKERHUB = credentials('docker-hub-credentials')
    GITHUB = credentials('github-jenkins-tahourdev')

    // App image & CD repo settings
    APP_IMAGE_REPO   = 'docker.io/keanghor31/product-service'
    CD_REPO_URL      = 'https://github.com/tahourdev/cd-product-service.git'
    CD_REPO_BRANCH   = 'main'
    CHART_PATH       = 'charts/product-service'  // path within Repo B
  }

  options {
    timestamps()
    disableConcurrentBuilds()
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
        sh 'git --version'
      }
    }

    stage('Compute Image Tag') {
      steps {
        script {
          env.SHORT_SHA   = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
          // Try to read Maven version if present; fall back to date
          def ver = sh(script: "grep -m1 '<version>' pom.xml | sed -E 's/.*<version>(.+)<\\/version>.*/\\1/' || true", returnStdout: true).trim()
          if (!ver) { ver = sh(script: "date +%Y.%m.%d", returnStdout: true).trim() }
          env.IMAGE_TAG = "${ver}-${env.SHORT_SHA}-${env.BUILD_NUMBER}"
          echo "IMAGE_TAG = ${env.IMAGE_TAG}"
        }
      }
    }

    stage('Build & Push Docker Image') {
      steps {
        sh '''
          echo "$DOCKERHUB_PSW" | docker login -u "$DOCKERHUB_USR" --password-stdin
          docker build -t ${APP_IMAGE_REPO}:${IMAGE_TAG} .
          docker push ${APP_IMAGE_REPO}:${IMAGE_TAG}
        '''
      }
    }

    stage('Update Helm values in Repo B') {
      steps {
        dir('cd-repo') {
          sh '''
            git config --global user.email "enghourheng26@gmail.com"
            git config --global user.name  "tahourdev"

            # Clone Repo B with embedded credentials (HTTPS)
            CLONE_URL="${CD_REPO_URL#https://}"
            git clone --depth 1 --branch ${CD_REPO_BRANCH} https://${GITHUB_USR}:${GITHUB_PSW}@${CLONE_URL} .

            VALUES_FILE="${CHART_PATH}/values.yaml"

            # Ensure keys exist
            grep -q "imageRepository" "$VALUES_FILE" || { echo "values.yaml missing image.imageRepository"; exit 1; }
            grep -q "tag:" "$VALUES_FILE" || { echo "values.yaml missing image.tag"; exit 1; }

            # Update repo and tag
            # imageRepository line: set to APP_IMAGE_REPO
            sed -i -E "s|(^\\s*imageRepository:\\s*).*|\\1${APP_IMAGE_REPO}|" "$VALUES_FILE"
            # tag line: replace quoted or unquoted value
            sed -i -E "s|(^\\s*tag:\\s*\").*(\"\\s*$)|\\1${IMAGE_TAG}\\2|; t; s|(^\\s*tag:\\s*).*|\\1\"${IMAGE_TAG}\"|" "$VALUES_FILE"

            git add "$VALUES_FILE"
            git commit -m "chore(ci): deploy ${APP_IMAGE_REPO}:${IMAGE_TAG} [skip ci]"
            git push origin ${CD_REPO_BRANCH}
          '''
        }
      }
    }
  }

  post {
    success {
      echo "✅ Pushed ${APP_IMAGE_REPO}:${IMAGE_TAG} and updated Repo B."
    }
    always {
      sh 'docker logout || true'
      archiveArtifacts artifacts: 'target/*.jar', fingerprint: true, onlyIfSuccessful: false
    }
  }
}
