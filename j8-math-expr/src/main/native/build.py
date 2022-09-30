from operator import indexOf
import os
import subprocess
import shutil

################################
# State
################################

class BuildTarget:
    def __init__(self, name : str, type : str):
        self.name = name
        self.type = type

class BuildState:
    def __init__(self):
        self.include_dirs = []
        self.lib_dirs     = []
        self.dependencies = []
        pass

    def prepare(self):
        if not os.path.exists(self.obj_dir):
            os.makedirs(self.obj_dir)
        if not os.path.exists(self.bin_dir):
            os.makedirs(self.bin_dir)

    def clean(self):
        shutil.rmtree(self.obj_dir)
        shutil.rmtree(self.bin_dir)

    def set_target(self, target):
        self.target = target
        return self

    def set_obj_dir(self, fn):
        self.obj_dir = fn
        return self

    def set_bin_dir(self, fn):
        self.bin_dir = fn
        return self

    def set_src_dir(self, fn):
        self.src_dir = fn
        return self

    def add_include_dir(self, fn):
        self.include_dirs.append(fn)
        return self

    def add_lib_dir(self, fn):
        self.lib_dirs.append(fn)
        return self

    def add_lib_file_dependency(self, fn):
        self.dependencies.append("lib_file://" + fn)

    def add_project_dir_dependency(self, fn):
        self.dependencies.append("project_dir://" + fn);

def get_platform_str(osName, arch):
    return arch + ("-" + osName if osName != None else "")

################################
# Compilation
################################

CXX_COMPILER = 'g++'
CXX_CFLAGS   = '-shared'

def get_obj_dir(state, target, osName, arch):
    return os.path.join(state.obj_dir, target.name + "-" + get_platform_str(osName, arch))

def get_obj_output(state : BuildState, target, rel_filename, osName, arch):
    # replace directory seperators
    rel_filename = rel_filename.replace("/",  "+")
    rel_filename = rel_filename.replace("\\", "+")
    # split text and add .o extension
    fn = os.path.splitext(rel_filename)[0] + ".o";
    # join with object directory
    return os.path.join(get_obj_dir(state, target, osName, arch), fn)

def compile_file(state : BuildState, target, filename, osName, arch):
    # build flags
    flags = []
    # append source file
    flags.append("-c " + filename)
    # append object output
    obj_out = get_obj_output(state, target, filename, osName, arch)
    flags.append("-o " + obj_out)
    # append architecture
    if arch == 'x64':
        flags.append("-m64")
    elif arch == 'x32':
        flags.append("-m32")
    # append platform defines
    if osName != None:
        flags.append("-D_OS " + osName)
    flags.append("-D_ARCH " + arch)
    # append user defined flags
    flags.append(CXX_CFLAGS)
    # stringify flags
    flagStr = ""
    for flag in flags:
        flagStr = flagStr + flag + " "
    # construct command
    cmd = CXX_COMPILER + " " + flagStr

    # call command
    print("Compiling '" + filename + "' " + target.name + " (" + target.type + ") " + get_platform_str(osName, arch))
    process = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    process.wait()

    # return code
    return process.returncode, cmd, filename, obj_out

################################
# Linking
################################

LD_EXE   = 'ld'
LD_FLAGS = '-shared'

def get_output_file_name(state : BuildState, target : BuildTarget, osName : str, arch):
    # get file extension
    ext = ""
    if target.type == 'executable':
        if osName.lower().find('win') != -1:
            ext = '.exe'
        else:
            ext = ''
    elif target.type.startswith('lib'):
        spl  = target.type.split(':')
        if len(spl) < 2:
            raise ValueError('Invalid target type: %s' % target) 
        spec = spl[1]
        if spec == 'static':
            ext = '.a'
        elif spec == 'dynamic':
            ext = '.so'
    # build final file name
    return target.name + "-" + get_platform_str(osName, arch) + "."

def link_target(state, target, osName, arch):
    # get obj file directory
    obj_dir = get_obj_dir(state, target, osName, arch)

    # build flags
    flags = []
    # append output file
    out_file = os.path.join(state.bin_dir, get_output_file_name(state, target, osName, arch))
    flags.append("-o " + out_file)
    # append all object files
    for of in os.listdir(obj_dir):
        if of.endswith(".o"):
            flags.append(os.path.join(obj_dir, of))
    # append user flags
    flags.append(LD_FLAGS)
    # stringify flags
    flagStr = ""
    for flag in flags:
        flagStr = flagStr + flag + " "
    # construct command
    cmd = LD_EXE + " " + flagStr

    # call command
    print("Linking " + target.name + " (" + target.type + ") " + get_platform_str(osName, arch) + " -> " + out_file)
    process = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    process.wait()

    # return exit
    return process.returncode, cmd, out_file
