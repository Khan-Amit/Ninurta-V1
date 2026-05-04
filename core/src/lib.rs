use jni::objects::{JClass, JString};
use jni::sys::{jboolean, jfloat, jint, jstring};
use jni::JNIEnv;
use std::fs::OpenOptions;
use std::io::{Write, Read, Seek, SeekFrom};
use std::path::Path;

#[no_mangle]
pub extern "system" fn Java_com_ninurta_MainActivity_nativeFormat(
    env: JNIEnv,
    _class: JClass,
    path: JString,
    size_mb: jint,
) -> jboolean {
    let path_str: String = env.get_string(&path).expect("Couldn't get path").into();
    let result = std::fs::File::create(&path_str)
        .and_then(|mut f| f.set_len(size_mb as u64 * 1024 * 1024));
    result.is_ok() as jboolean
}

#[no_mangle]
pub extern "system" fn Java_com_ninurta_MainActivity_nativeWrite(
    env: JNIEnv,
    _class: JClass,
    domain: jint,
    desire: jfloat,
    data: JString,
) -> jboolean {
    let data_str: String = env.get_string(&data).expect("Couldn't get data").into();
    // For prototype, we just append to a file "arena.log" in the app's private storage.
    // In a real implementation, you'd manage a circular buffer.
    let result = std::fs::OpenOptions::new()
        .create(true)
        .append(true)
        .open("/data/local/tmp/ninurta_arena.log")
        .and_then(|mut file| {
            writeln!(file, "{} {} {}", domain, desire, data_str)
        });
    result.is_ok() as jboolean
}

#[no_mangle]
pub extern "system" fn Java_com_ninurta_MainActivity_nativeRead(
    env: JNIEnv,
    _class: JClass,
    offset: jint,
) -> jstring {
    let mut file = std::fs::OpenOptions::new()
        .read(true)
        .open("/data/local/tmp/ninurta_arena.log")
        .unwrap();
    let mut content = String::new();
    use std::io::BufRead;
    let reader = std::io::BufReader::new(file);
    let line = reader.lines().nth(offset as usize).unwrap().unwrap();
    env.new_string(&line).unwrap().into_inner()
}
