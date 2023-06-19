import paramiko

def run_in_master(command: str, username: str = "root", password:str = "pass"):
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect("namenode", username=username, password=password)
    ssh_stdin, ssh_stdout, ssh_stderr = ssh.exec_command(f"cd /app/ && . /env_var_path.sh && {command}")
    return (ssh_stdout.readlines(), ssh_stderr.readlines())

def run_in_hive(command: str, username: str = "root", password:str="pass"):
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect("hive-server", username=username, password=password)
    ssh_stdin, ssh_stdout, ssh_stderr = ssh.exec_command(f"bash -c '. /env_var_path.sh && {command}'")
    return (ssh_stdout.readlines(), ssh_stderr.readlines())