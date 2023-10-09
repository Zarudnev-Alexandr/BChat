import React, { useState, useEffect } from 'react';

const Chat = () => {
  const [messages, setMessages] = useState([]);
  const [messageInput, setMessageInput] = useState('');
  const chatUrl = 'ws://localhost:8000/ws/send_message/1/1/';
  const websocket = new WebSocket(chatUrl);

  useEffect(() => {
    websocket.onopen = (event) => {
      console.log('WebSocket connection opened:', event);
    };

    websocket.onmessage = (event) => {
      const message = JSON.parse(event.data).message;
      setMessages([...messages, message]);
    };

    websocket.onclose = (event) => {
      if (event.wasClean) {
        console.log(`Closed cleanly, code=${event.code}, reason=${event.reason}`);
      } else {
        console.error(`Connection died`);
      }
    };

    websocket.onerror = (error) => {
      console.error('WebSocket Error:', error);
    };

    return () => {
      websocket.close();
    };
  }, [messages]);

  const sendMessage = () => {
    if (messageInput) {
      websocket.send(messageInput);
      setMessageInput('');
    }
  };

  return (
    <div>
      <div style={{ height: '300px', border: '1px solid #ccc', padding: '10px', overflowY: 'auto' }}>
        {messages.map((message, index) => (
          <div key={index}>{message}</div>
        ))}
      </div>
      <div style={{ marginTop: '10px' }}>
        <input type="text" value={messageInput} onChange={(e) => setMessageInput(e.target.value)} />
        <button onClick={sendMessage}>Send</button>
      </div>
    </div>
  );
};

export default Chat;
