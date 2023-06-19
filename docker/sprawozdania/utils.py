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

def print_hdfs_output(path, max_lines:int = 10):
    raw = run_in_master(f"hdfs dfs -cat {path}")[0]
    print("\n".join(raw[0:max_lines]))

def hdfs_mkdir(path):
    run_in_master(f"hdfs dfs -mkdir -p /{path}/")

def hdfs_upload(path):
    directory = "/".join(path.split("/")[:-1])
    hdfs_mkdir(directory)
    cmd = f"hdfs dfs -put /data/master_volume/{path} /{directory}"
    print(cmd)
    code, output = run_in_master(cmd)
    print(f"exit code {code}")
    print(output)