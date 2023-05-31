** Usage **

Jenkins Server - Windows
Remote Server  - Windows


# Connect to Remote Server
 
 - Bind with Drive Name (Z and X) 
 ** To separate from the Jenkins Server Drive and Remote Server Drive


# Create Folder and TakeBackup
 - Both Backup and Temp Folder will create.
    * Backup folder for Rollback and 
    * Temp Folder to keep the Deployment Packages.

# TakeBackup 
  - If the deployment failed  , we can rollback to original packages.

# Clone Git Repository
  - Clone the source code from GIT and keep those packages under D:\Temp\

# Deployment
    [ Most Important Part of this pipeline. Need to check Path and Packages before Deployment ]

# Rollback
    