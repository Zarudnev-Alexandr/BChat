export const register = async (data) => {
  try {
    const response = await fetch('http://localhost:8000/api/users/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });

    const result = await response.json();
    return result;
  } catch (error) {
    console.error(error);
    throw error;
  }
};


export const login = async (data) => {
  const formData = new FormData();
  formData.append("email", data.email);
  formData.append("password", data.password);

  try {
    const response = await fetch('http://localhost:8000/api/users/login/', {
      method: 'POST',
      body: formData
    });

    const result = await response.json();
    localStorage.setItem("token", result.access_token);
    return result;
  } catch (error) {
    console.error(error);
    throw error;
  }
};
