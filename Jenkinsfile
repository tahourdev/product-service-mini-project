pipeline {
  agent { label "agent-2" }

  environment {
    // ---- Image settings ----
    DOCKER_HUB_REPO           = 'keanghor31/spring-app01'
    DOCKER_HUB_CREDENTIALS_ID = 'docker-hub-credentials'
    BASE_VERSION              = '1.0'

    // ---- GitOps (manifests) repo (Helm chart + values files) ----
    // Use your GitOps repo here (matches your logs):
    GITOPS_URL            = 'https://github.com/tahourdev/Jenkins-ArgoCD-GitOps.git'
    GITOPS_BRANCH         = 'main'
    DEV_VALUES_FILE       = 'manifests/spring-jpa-helm/values-dev.yaml'
    GITOPS_CREDENTIALS_ID = 'github-jenkins-token'   // PAT with Contents: Read & write

    // ---- Commit identity for GitOps changes ----
    GIT_USER_NAME         = 'tahourdev'
    GIT_USER_EMAIL        = 'enghourheng26@gmail.com'
  }

  stages {

    stage('Checkout app') {
      steps { checkout scm }
    }

    stage('Build Spring Boot') {
      steps {
        sh '''
          set -e
          chmod +x ./gradlew || true
          ./gradlew clean build -x test
        '''
      }
    }

    stage('Build & Push image') {
      steps {
        script {
          def shortSha = sh(returnStdout:true, script:"git rev-parse --short HEAD").trim()
          def imageTag = "${BASE_VERSION}.${env.BUILD_NUMBER}-${shortSha}"
          env.IMAGE_TAG = imageTag

          echo "📦 Building ${DOCKER_HUB_REPO}:${imageTag}"
          def img = docker.build("${DOCKER_HUB_REPO}:${imageTag}")

          echo "🚀 Pushing to Docker Hub"
          docker.withRegistry('', "${DOCKER_HUB_CREDENTIALS_ID}") {
            img.push("${imageTag}")
            img.push("latest")
          }
        }
      }
    }

    stage('Trivy scan (image)') {
      steps {
        sh '''
          set -e
          docker run --rm -v /var/run/docker.sock:/var/run/docker.sock aquasec/trivy image \
            --severity HIGH,CRITICAL --no-progress --format table \
            -o trivy-scan-report.txt "$DOCKER_HUB_REPO:$IMAGE_TAG" || echo "Trivy scan failed but continuing..."
        '''
      }
    }

    stage('Bump Helm values-dev.yaml in GitOps repo') {
      steps {
        withCredentials([usernamePassword(
          credentialsId: "${GITOPS_CREDENTIALS_ID}",
          usernameVariable: 'GIT_USER',
          passwordVariable: 'GIT_TOKEN'
        )]) {
          sh '''
            set -e

            # start clean each run
            rm -rf gitops-tmp

            # secure clone using a throwaway credential helper (no token in logs)
            git -c credential.helper='!f() { echo username='${GIT_USER}'; echo password='${GIT_TOKEN}'; }; f' \
                clone "${GITOPS_URL}" -b "${GITOPS_BRANCH}" gitops-tmp

            cd gitops-tmp
            git config user.name  "${GIT_USER_NAME}"
            git config user.email "${GIT_USER_EMAIL}"

            echo "📝 Updating image tag in ${DEV_VALUES_FILE} -> ${IMAGE_TAG}"

            # update `tag:` line (keeps YAML formatting simple)
            sed -i -E "s#(^\\s*tag:\\s*).*$#\\1 \\\"${IMAGE_TAG}\\\"#" "${DEV_VALUES_FILE}"

            git add "${DEV_VALUES_FILE}"
            git commit -m "ci(dev): bump image to ${DOCKER_HUB_REPO}:${IMAGE_TAG}" || echo "Nothing to commit"

            # push back using same credential helper
            git -c credential.helper='!f() { echo username='${GIT_USER}'; echo password='${GIT_TOKEN}'; }; f' \
                push origin HEAD:"${GITOPS_BRANCH}"
          '''
        }
      }
    }

    stage('(Info) Argo CD auto-sync') {
      steps {
        echo '🧠 Dev app in Argo CD will auto-sync when the values-dev.yaml change is pushed.'
      }
    }
  }

  post {
    success {
      echo '✅ Pipeline done: image pushed & GitOps updated. Argo CD should roll out dev automatically.'
      archiveArtifacts artifacts: 'trivy-scan-report.txt', allowEmptyArchive: true
    }
    failure {
      echo '❌ Pipeline failed.'
      archiveArtifacts artifacts: 'trivy-scan-report.txt', allowEmptyArchive: true
    }
  }
}
