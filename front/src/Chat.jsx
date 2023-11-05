import React, { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { register, login } from './http/http'

const Chat = () => {
  //   const [messages, setMessages] = useState([]);
  //   const [messageInput, setMessageInput] = useState('');
  //   const chatUrl = 'ws://localhost:8000/ws/send_message/1/1/';
  //   const websocket = new WebSocket(chatUrl);

  //   useEffect(() => {
  //     websocket.onopen = (event) => {
  //       console.log('WebSocket connection opened:', event);
  //     };

  //     websocket.onmessage = (event) => {
  //       const message = JSON.parse(event.data).message;
  //       setMessages([...messages, message]);
  //     };

  //     websocket.onclose = (event) => {
  //       if (event.wasClean) {
  //         console.log(`Closed cleanly, code=${event.code}, reason=${event.reason}`);
  //       } else {
  //         console.error(`Connection died`);
  //       }
  //     };

  //     websocket.onerror = (error) => {
  //       console.error('WebSocket Error:', error);
  //     };

  //     return () => {
  //       websocket.close();
  //     };
  //   }, [messages]);

  //   const sendMessage = () => {
  //     if (messageInput) {
  //       websocket.send(messageInput);
  //       setMessageInput('');
  //     }
  //   };

  //   return (
  //     <div>
  //       <div style={{ height: '300px', border: '1px solid #ccc', padding: '10px', overflowY: 'auto' }}>
  //         {messages.map((message, index) => (
  //           <div key={index}>{message}</div>
  //         ))}
  //       </div>
  //       <div style={{ marginTop: '10px' }}>
  //         <input type="text" value={messageInput} onChange={(e) => setMessageInput(e.target.value)} />
  //         <button onClick={sendMessage}>Send</button>
  //       </div>
  //     </div>
  //   );

  const { handleSubmit, reset } = useForm({ mode: 'onBlur' });

  let [registerData, setRegisterData] = useState({})
  let [loginData, setLoginData] = useState({})
  let [auth, setAuth] = useState()

  useEffect(() => {

  }, [])

  let handleRegister = (e) => {
    e.preventDefault();
    register(registerData)
  }

  let handleLogin = (e) => {
    e.preventDefault();
    login(loginData)
  }

  return (
    <>
      <form onSubmit={e => handleSubmit(handleRegister(e))}>
        <h2>Регистрация</h2>
        <input name="email" type='text' onChange={e => setRegisterData({ ...registerData, email: e.target.value })} />
        <input name='hashed_password' type="password" onChange={e => setRegisterData({ ...registerData, hashed_password: e.target.value })} />
        <input name='nickname' type="text" onChange={e => setRegisterData({ ...registerData, nickname: e.target.value })} />
        <input name='name' type="text" onChange={e => setRegisterData({ ...registerData, name: e.target.value })} />
        <input name='surname' type="text" onChange={e => setRegisterData({ ...registerData, surname: e.target.value })} />
        <input name='date_of_birth' type="text" onChange={e => setRegisterData({ ...registerData, date_of_birth: e.target.value })} />
        <input name='imageURL' type="text" onChange={e => setRegisterData({ ...registerData, imageURL: e.target.value })} />
        <button type='submit'>Регстрация</button>
      </form>
      <form onSubmit={e => handleSubmit(handleLogin(e))}>
        <h2>Логин</h2>
        <input name="email" type='text' onChange={e => setLoginData({ ...loginData, email: e.target.value })} />
        <input name='password' type="password" onChange={e => setLoginData({ ...loginData, password: e.target.value })} />
        <button type='submit'>логин</button>
      </form>
    </>
  )
};

export default Chat;
